package com.imjasonh.partychapp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.xmpp.JID;
import com.google.appengine.repackaged.com.google.common.collect.Lists;
import com.google.appengine.repackaged.com.google.common.collect.Sets;
import com.imjasonh.partychapp.Member.SnoozeStatus;

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
  private boolean inviteOnly = false;

  @Persistent
  private List<String> invitedIds;
  
  public Channel(JID serverJID) {
    this.name = serverJID.getId().split("@")[0];
    members = Sets.newHashSet();
    invitedIds = Lists.newArrayList();
  }

  public void invite(String email) {
    // Need to be robust b/c invitees was added after v1 of this class.
    if (invitedIds == null) {
      invitedIds = Lists.newArrayList();
    }
    invitedIds.add(email.toLowerCase().trim());
  }

  public boolean canJoin(String email) {
    return !inviteOnly ||
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
  public void addMember(Member member) {
    if (inviteOnly) {
      if (invitedIds == null || !invitedIds.remove(member.getJID())) {
        throw new IllegalArgumentException("Not invited to this room");
      }
    }
    while (null != getMemberByAlias(member.getAlias())) {
      member.setAlias("_" + member.getAlias());
    }
    members.add(member);
    // I feel dirty doing this! There is some opaque JDO bug that makes
    // this not save.
    JDOHelper.makeDirty(this, "members");
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

  public Set<Member> awakenSnoozers() {
    Set<Member> awoken = Sets.newHashSet();
    for (Member member : getMembers()) {
      if (member.getSnoozeStatus() == SnoozeStatus.SHOULD_WAKE) {
        member.setSnoozeUntil(null);
        awoken.add(member);
      }
    }
    return awoken;
  }

  public JID[] getMembersJIDsToSendTo() {
    return getMembersJIDsToSendTo(null);
  }

  /**
   * @param exclude
   *          a JID to exclude (for example the person sending the broadcast message)
   * @return an array of JIDs to send a message to, excluding snoozing members.
   */
  public JID[] getMembersJIDsToSendTo(JID exclude) {
    String excludeJID = (exclude != null) ? exclude.getId().split("/")[0] : null;
    ArrayList<JID> jids = Lists.newArrayList();
    for (Member member : getMembers()) {
      if (!member.getJID().equals(excludeJID)
          && member.getSnoozeStatus() != SnoozeStatus.SNOOZING) {
        jids.add(new JID(member.getJID()));
      }
    }
    
    if (jids.isEmpty()) {
      return null;
    }

    JID returnJids[] = new JID[jids.size()];
    jids.toArray(returnJids);
    return returnJids;
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
    Datastore.instance().put(this);
  }

  public void delete() {
    Datastore.instance().delete(this);
  }

  public boolean isInviteOnly() {
    return inviteOnly;
  }
  
  public List<String> getInvitees() {
    return invitedIds != null ? invitedIds : Lists.<String>immutableList();
  }
}
