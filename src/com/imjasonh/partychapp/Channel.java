package com.imjasonh.partychapp;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.xmpp.JID;
import com.google.appengine.repackaged.com.google.common.collect.ImmutableList;
import com.google.appengine.repackaged.com.google.common.collect.Lists;
import com.google.appengine.repackaged.com.google.common.collect.Sets;
import com.imjasonh.partychapp.Member.SnoozeStatus;
import com.imjasonh.partychapp.server.SendUtil;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Channel implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 3860339764413214817L;

  // private static final Logger LOG = Logger.getLogger(Channel.class.getName());

  @PrimaryKey
  @Persistent
  private String name;

  @Persistent(serialized = "true")
  private Set<Member> members;

  @Persistent
  private Boolean inviteOnly = false;

  @Persistent
  private List<String> invitedIds;
  
  @Persistent
  private String serverJID = null;
  
  @Persistent
  private Integer sequenceId = 0;
  
  public Channel(JID serverJID) {
    this.serverJID = serverJID.getId();
    this.name = serverJID.getId().split("@")[0];
    members = Sets.newHashSet();
    invitedIds = Lists.newArrayList();
    this.sequenceId = 0;
  }
  
  public Channel(Channel other) {
    this.name = other.name;
    this.inviteOnly = other.inviteOnly;
    this.invitedIds = Lists.newArrayList(other.invitedIds);
    this.members = Sets.newHashSet();
    for (Member m : other.getMembers()) {
      this.members.add(new Member(m));
    }
    this.serverJID = other.serverJID;
    this.sequenceId = other.sequenceId;
  }
  
  public JID serverJID() {
    // this is a new addition, so pre-existing rooms won't have this field
    if (serverJID == null) {
      serverJID = name + "@partychapp.appspotchat.com";
    }
    return new JID(serverJID);
  }

  public void invite(String email) {
    // Need to be robust b/c invitees was added after v1 of this class.
    if (invitedIds == null) {
      invitedIds = Lists.newArrayList();
    }
    String cleanedUp = email.toLowerCase().trim();
    if (!invitedIds.contains(cleanedUp)) {
      invitedIds.add(cleanedUp);
    }
  }

  public boolean canJoin(String email) {
    return !isInviteOnly() ||
        (invitedIds != null && invitedIds.contains(email.toLowerCase().trim()));
  }
  
  public void setInviteOnly(boolean inviteOnly) {
    this.inviteOnly = inviteOnly;
  }
  
  /**
   * Adds a member to the channel. This may alter the member's alias by
   * prepending a _ if the channel already has a member with that alias. Removes
   * from invite list if invite-only room.
   */
  public Member addMember(JID jidToAdd) {
    if (isInviteOnly()) {
      String email = jidToAdd.getId().split("/")[0];
      if (invitedIds == null || !invitedIds.remove(email)) {
        throw new IllegalArgumentException("Not invited to this room");
      }
    }
    Member addedMember = new Member(this, jidToAdd);
    String dedupedAlias = addedMember.getAlias();
    while (null != getMemberByAlias(dedupedAlias)) {
      dedupedAlias = "_" + dedupedAlias;
    }
    addedMember.setAlias(dedupedAlias);
    members.add(addedMember);
    // I feel dirty doing this! There is some opaque JDO bug that makes
    // this not save.
    JDOHelper.makeDirty(this, "members");
    return addedMember;
  }
  
  private Set<Member> mutableMembers() {
    if (members == null) {
      members = Sets.newHashSet();
    }
    return members;
  }

  public void removeMember(Member member) {
    mutableMembers().remove(member);
    // I feel dirty doing this! There is some opaque JDO bug that makes
    // this not save.
    JDOHelper.makeDirty(this, "members");
  }

  public List<Member> getMembersToSendTo() {
    return getMembersToSendTo(null);
  }

  /**
   * @param exclude
   *          a JID to exclude (for example the person sending the broadcast message)
   * @return an array of JIDs to send a message to, excluding snoozing members.
   */
  public List<Member> getMembersToSendTo(Member exclude) {
    List<Member> recipients = Lists.newArrayList();
    for (Member member : getMembers()) {
      if (!member.equals(exclude)
          && member.getSnoozeStatus() != SnoozeStatus.SNOOZING) {
        recipients.add(member);
      }
    }
    
    return recipients;
  }

  public String getName() {
    return name;
  }

  public Set<Member> getMembers() {
    return Collections.unmodifiableSet(mutableMembers());
  }

  public Member getMemberByJID(JID jid) {
    String shortJID = jid.getId().split("/")[0];
    for (Member member : getMembers()) {
      if (member.getJID().equals(shortJID)) {
        return member;
      }
    }
    return null;
  }

  public Member getMemberByAlias(String alias) {
    for (Member member : getMembers()) {
      if (member.getAlias().equals(alias)) {
        return member;
      }
    }
    return null;
  }

  /**
   * Remove a user or invitee by alias or ID.
   * @return True if someone was removed
   */
  public boolean kick(String id) {
    Member member = getMemberByAlias(id);
    if (member == null) {
      member = getMemberByJID(new JID(id));
    }
    if (member != null) {
      removeMember(member);
      return true;
    }
    if (invitedIds.remove(id)) {
      return true;
    }
    return false;
  }

  public void put() {
    // I feel dirty doing this! There is some opaque JDO bug that makes
    // this not save.
    JDOHelper.makeDirty(this, "members");
    Datastore.instance().put(this);
  }

  public void delete() {
    Datastore.instance().delete(this);
  }

  public boolean isInviteOnly() {
    return inviteOnly != null && inviteOnly;
  }
  
  public List<String> getInvitees() {
    return invitedIds != null ? invitedIds : ImmutableList.<String>of();
  }
  
  public void sendMessage(String message, List<Member> recipients) {
    List<JID> recipientJIDS = Lists.newArrayList();
    List<JID> recipientJIDSWithSequenceId = Lists.newArrayList();
    for (Member m : recipients) {
      if (m.debugOptions().isEnabled("sequenceIds")) {
        recipientJIDSWithSequenceId.add(new JID(m.getJID()));
      } else {
        recipientJIDS.add(new JID(m.getJID()));
      }
    }
    SendUtil.sendMessage(message, serverJID(), recipientJIDS.toArray(new JID[]{}));
    SendUtil.sendMessage(message + " (" + sequenceId + ")",
                         serverJID(),
                         recipientJIDSWithSequenceId.toArray(new JID[]{}));
  }

  public void broadcast(String message, Member sender) {
    incrementSequenceId();
    awakenSnoozers();
    put();
    sendMessage(message, getMembersToSendTo(sender));
  }
  
  public void broadcastIncludingSender(String message) {
    incrementSequenceId();
    awakenSnoozers();
    put();
    sendMessage(message, getMembersToSendTo());    
  }

  private void awakenSnoozers() {
    // awaken snoozers and broadcast them awaking.
    Set<Member> awoken = Sets.newHashSet();
    for (Member member : getMembers()) {
      if (member.getSnoozeStatus() == SnoozeStatus.SHOULD_WAKE) {
        member.setSnoozeUntil(null);
        awoken.add(member);
      }
    }
    
    if (!awoken.isEmpty()) {
      put();
      StringBuilder sb = new StringBuilder().append("Members unsnoozed:");
      for (Member m : awoken) {
        sb.append('\n').append(m.getAlias());
      }
      broadcastIncludingSender(sb.toString());
    }
  }
  
  public void incrementSequenceId() {
    if (sequenceId == null) {
      sequenceId = 0;
    }
    ++sequenceId;
    if (sequenceId >= 100) {
      sequenceId = 0;
    }
  }
}