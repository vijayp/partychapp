package com.imjasonh.partychapp;

import java.io.Serializable;
import java.util.Collection;
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
import com.imjasonh.partychapp.server.MailUtil;
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
  private Set<Member> members =  Sets.newHashSet();
  
  @Persistent
  private Boolean inviteOnly = false;

  @Persistent
  private List<String> invitedIds = Lists.newArrayList();
  
  @Persistent
  private Integer sequenceId = 0;
  
  public Channel(JID serverJID) {
    this.name = serverJID.getId().split("@")[0];
  }
   
  public Channel(Channel other) {
    this.name = other.name;
    this.inviteOnly = other.inviteOnly;
    this.invitedIds = Lists.newArrayList(other.invitedIds);
    this.members = Sets.newHashSet();
    for (Member m : other.members) {
      this.members.add(new Member(m));
    }
    this.sequenceId = other.sequenceId;
  }
  
  public JID serverJID() {
    return new JID(name + "@" + Configuration.chatDomain);
  }
  
  public String mailingAddress() {
    return name + "@" + Configuration.mailDomain;
  }

  public void invite(String email) {
    // Need to be robust b/c invitees was added after v1 of this class.
    String cleanedUp = email.toLowerCase().trim();
    if (!invitedIds.contains(cleanedUp)) {
      invitedIds.add(cleanedUp);
    }
  }

  public boolean canJoin(String email) {
    return !isInviteOnly() ||
        (invitedIds.contains(email.toLowerCase().trim()));
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
    String jidNoResource = jidToAdd.getId().split("/")[0];
    String email = jidNoResource;
    if (invitedIds == null || !invitedIds.remove(email.toLowerCase())) {
      if (isInviteOnly()) {
        throw new IllegalArgumentException("Not invited to this room");
      }
    }
    Member addedMember = new Member(this, Datastore.instance().getOrCreateUser(jidNoResource));
    String dedupedAlias = addedMember.getAlias();
    while (null != getMemberByAlias(dedupedAlias)) {
      dedupedAlias = "_" + dedupedAlias;
    }
    addedMember.setAlias(dedupedAlias);
    mutableMembers().add(addedMember);

    // I feel dirty doing this! There is some opaque JDO bug that makes
    // this not save.
    JDOHelper.makeDirty(this, "members");
    
    addedMember.user().addChannel(getName());
    
    return addedMember;    
  }
  
  private Set<Member> mutableMembers() {
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
 
  public Member getOrSuggestMemberFromUserInput(String input, StringBuilder suggestion) {
    Member found = getMemberByAlias(input);
    if (found != null) {
      return found;
    }
    if (input.indexOf('@') != -1) {
      found = getMemberByJID(new JID(input));
      if (found != null) {
        return found;
      }
    } else {
      for (Member m : getMembers()) {
        if (m.getJID().startsWith(input + "@")) {
          return m;
        }
      }
    }

    suggestion.append("Could not find member with input '" + input + ".'");
    for (Member m : getMembers()) {
      if (m.getAlias().contains(input) ||
          m.getJID().contains(input)) {
        suggestion.append(" Maybe you meant '" + m.getAlias() + ".'");
      }
    }
    return null;
  }
 
  
  public Member getMemberByPhoneNumber(String phoneNumber) {
    for (Member member : getMembers()) {
      String memberPhone = member.user().phoneNumber();
      if ((memberPhone != null) && memberPhone.equals(phoneNumber)) {
        return member;
      }
    }
    if (phoneNumber.startsWith("1")) {
      return getMemberByPhoneNumber(phoneNumber.substring(1));
    } else {
      return getMemberByPhoneNumber("1" + phoneNumber);
    }
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
    return inviteOnly;
  }
 
  public List<String> getInvitees() {
    return invitedIds;
  }
  
  private void sendMessage(String message, List<Member> recipients) {
    incrementSequenceId();
    awakenSnoozers();
    put();

    String messageWithSequenceId = message + " (" + sequenceId + ")";

    List<JID> withSequenceId = Lists.newArrayList();
    List<JID> noSequenceId = Lists.newArrayList();
    for (Member m : recipients) {
      if (m.debugOptions().isEnabled("sequenceIds")) {
        withSequenceId.add(new JID(m.getJID()));
      } else {
        noSequenceId.add(new JID(m.getJID()));
      }
    }

    SendUtil.sendMessage(message, serverJID(), noSequenceId);
    SendUtil.sendMessage(messageWithSequenceId, serverJID(),
                         withSequenceId);
  }
  
  public void sendDirect(String message, Member recipient) {
    SendUtil.sendMessage(message,
                         serverJID(),
                         Lists.newArrayList(new JID(recipient.getJID())));
  }
  
  public void broadcast(String message, Member sender) {
    sendMessage(message, getMembersToSendTo(sender));
  }

  public void broadcastIncludingSender(String message) {
    sendMessage(message, getMembersToSendTo());
  }
  
  public String sendMail(String subject,
                         String body,
                         String recipient) {
    return MailUtil.sendMail(subject, body, this.mailingAddress(), recipient);
  }

  public List<Member> sendSMS(String body, Collection<Member> recipients) {
    List<Member> realRecipients = Lists.newArrayList();
    List<String> addresses = Lists.newArrayList();
    for (Member m : recipients) {
      if (m.user().canReceiveSMS()) {
        addresses.add(m.user().carrier().emailAddress(m.user().phoneNumber()));
        realRecipients.add(m);
      }
    }

    for (String addr : addresses) {
      sendMail("(sent from partychat)",
               body,
               addr);
    }
    
    return realRecipients;
  }
  
  public List<Member> broadcastSMS(String body) {
    return sendSMS(body, getMembers());
  }
  
  private void awakenSnoozers() {
    // awaken snoozers and broadcast them awaking.
    Set<Member> awoken = Sets.newHashSet();
    for (Member member : getMembers()) {
      if (member.unsnoozeIfNecessary()) {
        awoken.add(member);
      }
    }
    
    if (!awoken.isEmpty()) {
      put();
      StringBuilder sb = new StringBuilder();
      for (Member m : awoken) {
        if (sb.length() > 0) {
          sb.append("\n");
        }
        sb.append("_" + m.getAlias() + " is no longer snoozing_");
      }
      broadcastIncludingSender(sb.toString());
    }
  }

  public void incrementSequenceId() {
    ++sequenceId;
    if (sequenceId >= 100) {
      sequenceId = 0;
    }
  }
  
  public void fixUp() {
    boolean shouldPut = false;
    if (sequenceId == null) {
      sequenceId = 0;
      shouldPut = true;
    }
    if (members == null) {
      members = Sets.newHashSet();
      shouldPut = true;
    }
    if (inviteOnly == null) {
      inviteOnly = false;  
      shouldPut = true;
    }
    if (invitedIds == null) {
      invitedIds = Lists.newArrayList();
      shouldPut = true;
    }
    for (Member m : mutableMembers()) {
      invitedIds.remove(m.getJID().toLowerCase());
      if (m.fixUp(this)) {
        shouldPut = true;
      }
    }
    
    if (shouldPut) {
      put();
    }
  }
}