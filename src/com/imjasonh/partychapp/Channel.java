package com.imjasonh.partychapp;

import com.google.appengine.api.datastore.AsyncDatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.xmpp.JID;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import com.imjasonh.partychapp.DebuggingOptions.Option;
import com.imjasonh.partychapp.Member.SnoozeStatus;
import com.imjasonh.partychapp.server.MailUtil;
import com.imjasonh.partychapp.server.PartychappServlet;
import com.imjasonh.partychapp.server.SendUtil;
import com.imjasonh.partychapp.server.live.ChannelUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import javax.jdo.JDOHelper;
import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;


@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Channel implements Serializable {
  private static final Random randomGenerator = new Random();

  private static final Logger logger = 
      Logger.getLogger(Channel.class.getName());

  /**
   * Channels with more than this many members may have slightly different
   * behavior.
   */
  private static final int LARGE_CHANNEL_THRESHOLD = 50;

  @PrimaryKey
  @Persistent
  private String name;

  @Persistent
  private Boolean migrated=new Boolean(false);

  @Persistent(serialized = "true")
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value="true")
  private Set<Member> members = Sets.newHashSet();

  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value="true")
  private Boolean inviteOnly = false;

  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value="true")
  private List<String> invitedIds = Lists.newArrayList();

  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value="true")
  private Integer sequenceId = 0;

  /** 
   * Email addresses of users that have requested invitations.
   */
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value="true")
  private List<String> requestedInvitations = Lists.newArrayList();

  /**
   * Turns off storing of recent messages for the room. 
   */
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value="true")
  private Boolean loggingDisabled = false;

  public Channel(JID serverJID) {
    this.name = serverJID.getId().split("@")[0];
  }


  /// TODO(vijayp): this is really horribly horribly ugly
  // and should be moved somewhere way better, like the Channel class,
  // or maybe somewhere in persistent config.


  public boolean isMigrated() {
    return (true == this.migrated);
  }
  public void setMigrated(boolean m) {
    this.migrated = new Boolean(m);
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
    this.loggingDisabled = other.loggingDisabled;
  }

  public JID serverJID() {
    return new JID(serverJIDAsString());
  }

  public String serverJIDAsString() {
    return name + "@" + Configuration.chatDomain;
  }

  public String mailingAddress() {
    return name + "@" + Configuration.mailDomain;
  }

  public String webUrl() {
    return "http://" + Configuration.webDomain + "/room/" + name;
  }

  public void invite(String email) {
    // Need to be robust b/c invitees was added after v1 of this class.
    String cleanedUp = email.toLowerCase().trim();
    if (!invitedIds.contains(cleanedUp)) {
      invitedIds.add(cleanedUp);
    }
    requestedInvitations.remove(cleanedUp);
  }

  public boolean canJoin(String email) {
    return !isInviteOnly() ||
        (invitedIds.contains(email.toLowerCase().trim()));
  }

  public void setInviteOnly(boolean inviteOnly) {
    this.inviteOnly = inviteOnly;
  }

  public void setLoggingDisabled(boolean loggingDisabled) {
    this.loggingDisabled = loggingDisabled;
    if (loggingDisabled) {
      // Clear currently logged messages if we're disabling logging.
      fixUp();
    }
  }

  /**
   * Adds a member to the channel. This may alter the member's alias by
   * prepending a _ if the channel already has a member with that alias. Removes
   * from invite list if invite-only room.
   */
  public Member addMember(User userToAdd) {
    String jidNoResource = userToAdd.getJID().split("/")[0];
    String email = jidNoResource;
    if (invitedIds == null || !invitedIds.remove(email.toLowerCase())) {
      if (isInviteOnly()) {
        throw new IllegalArgumentException("Not invited to this room");
      }
    }
    Member addedMember = new Member(this, jidNoResource);
    String dedupedAlias = addedMember.getAlias();
    while (null != getMemberByAlias(dedupedAlias)) {
      dedupedAlias = "_" + dedupedAlias;
    }
    addedMember.setAlias(dedupedAlias);
    mutableMembers().add(addedMember);

    // I feel dirty doing this! There is some opaque JDO bug that makes
    // this not save.
    JDOHelper.makeDirty(this, "members");

    userToAdd.addChannel(getName());
    userToAdd.put();

    return addedMember;    
  }

  private Set<Member> mutableMembers() {
    return members;
  }

  public void removeMember(User userToRemove) {
    Member memberToRemove = getMemberByLiteralJID(userToRemove.getJID());
    if (!mutableMembers().remove(memberToRemove)) {
      logger.warning(
          userToRemove.getJID() + " was not actually in channel " +
              getName() + " when removing");
    }
    // I feel dirty doing this! There is some opaque JDO bug that makes
    // this not save.
    JDOHelper.makeDirty(this, "members");

    userToRemove.removeChannel(getName());
    userToRemove.put();
  }

  private List<Member> getMembersToSendTo() {
    return getMembersToSendTo(null);
  }

  /**
   * @param exclude
   *          a JID to exclude (for example the person sending the broadcast message)
   * @return an array of JIDs to send a message to, excluding snoozing members.
   */
  private List<Member> getMembersToSendTo(Member exclude) {
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
    return getMemberByJID(jid.getId());
  }

  public Member getMemberByJID(String jid) {
    String shortJID = jid.split("/")[0];
    for (Member member : getMembers()) {
      if (member.getJID().equalsIgnoreCase(shortJID)) {
        return member;
      }
    }
    return null;
  }

  /**
   * Unlike {@link Channel#getMemberByJID(JID)}, does a literal (case-sensitive)
   * comparison of JIDs. This should only be used when merging or removing users
   * from channels.
   */
  Member getMemberByLiteralJID(String jid) {
    String shortJID = jid.split("/")[0];
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
      User memberUser = Datastore.instance().getUserByJID(member.getJID());
      String memberPhone = memberUser.phoneNumber();
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
      removeMember(Datastore.instance().getUserByJID(member.getJID()));
      return true;
    }
    if (invitedIds.remove(id)) {
      return true;
    }
    if (requestedInvitations.remove(id)) {
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

  public boolean isLoggingDisabled() {
    return loggingDisabled;
  }

  public boolean isInviteOnly() {
    return inviteOnly;
  }

  public List<String> getInvitees() {
    return invitedIds;
  }

  public void removeInvitee(String invitee) {
    invitedIds.remove(invitee.toLowerCase().trim());
  }  

  public List<String> getRequestedInvitations() {
    return requestedInvitations;
  }

  public boolean hasRequestedInvitation(String email) {
    return requestedInvitations.contains(email.toLowerCase().trim());
  }

  public void addRequestedInvitation(String email) {
    String cleanedUp = email.toLowerCase().trim();
    if (!requestedInvitations.contains(cleanedUp)) {
      requestedInvitations.add(cleanedUp);
    }
  }    

  private void sendMessage(String message, List<Member> recipients) {

    try {
      if (this.isMigrated()) {
        logger.info("MIGRATED message");
        JSONObject jso = new JSONObject();

        jso.put("outmsg", message);
        List<String> rec = new ArrayList<String>();
        for (Member recipient : recipients) {
          rec.add(recipient.getJID().toString());
        }
        
        
        // if there are lots of people in a channel,
        // the addresses can take up most of the max space for xmpp
        // which is 32k. 
        jso.put("recipients", rec);
        jso.put("from_channel", this.name);
        
        String out = jso.toString();
        final int COMPRESS_THRESHOLD_BYTES = 0;//30000;
        try{
        if (out.length() > (COMPRESS_THRESHOLD_BYTES)) {
          // compress this packet
          
          ByteArrayOutputStream bos = new ByteArrayOutputStream();
          bos.write("gzip:".toString().getBytes());
          //Deflater dfl = new Deflater();
          DeflaterOutputStream dflOutStream = 
              new DeflaterOutputStream(bos);
          dflOutStream.write(out.getBytes());
          dflOutStream.close();
          bos.close();
          out = bos.toString();
        }
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
          logger.warning("couldn't compress data");
        } 
        
        logger.info("Sending raw message" + out + "to " 
            + PartychappServlet.PROXY_CONTROL);
        boolean succ = ChannelUtil.sendMessage(out, 
            PartychappServlet.PROXY_CONTROL,
            PartychappServlet.PARTYCHAPP_CONTROL);
        logger.info("Sent message to proxy control " + succ);
        
        
        // REMOVE THIS FOR DUAL BROADCASTS.
        return; 
        //
        //
        
        //if (message.isEmpty()) message = "<debug: refresh>";
      }
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }


    List<JID> withSequenceId = Lists.newArrayList();
    List<JID> noSequenceId = Lists.newArrayList();
    for (Member m : recipients) {
      if (m.debugOptions().isEnabled(Option.SEQUENCE_IDS)) {
        withSequenceId.add(new JID(m.getJID()));
      } else {
        noSequenceId.add(new JID(m.getJID()));
      }
    }

    // For small channels, also send messages to all invitees. That way as soon 
    // as they accept the chat request, they'll start getting messages, even 
    // before they message the bot and are added to the room in JoinCommand.
    if (members.size() < LARGE_CHANNEL_THRESHOLD) {
      for (String invitee : getInvitees()) {
        noSequenceId.add(new JID(invitee));
      }
    }

    Set<JID> errorJIDs = sendMessage(message, withSequenceId, noSequenceId);

    for (JID errorJID : errorJIDs) {
      // Skip over invitees, they're not members and so don't have debug options
      if (invitedIds.contains(errorJID.getId())) {
        continue;
      }
      Member member = getMemberByJID(errorJID);
      if (member == null) {
        logger.warning(
            "Could not find member " + errorJID.getId() + " in channel" + name);
        continue;
      }
      if (member.debugOptions().isEnabled(Option.ERROR_NOTIFICATIONS)) {
        sendDirect(
            "Attempted to send \"" + message + "\" to you but got an error",
            member);
      }
    }

    // TODO(mihaip): add uniform interface for XMPP and Channel endpoints, so
    // that Channel doesn't have to know about either SendUtil or ChannelUtil.

    for (Member recipient : recipients) {
      ChannelUtil.sendMessage(this, recipient, message);
    }
  }

  private Set<JID> sendMessage(
      String message, List<JID> withSequenceId, List<JID> noSequenceId) {
    incrementSequenceId();
    awakenSnoozers();

    String messageWithSequenceId = message + " (" + sequenceId + ")";

    Set<JID> errorJIDs = Sets.newHashSet();
    errorJIDs.addAll(SendUtil.sendMessage(message, serverJID(), noSequenceId));
    errorJIDs.addAll(
        SendUtil.sendMessage(messageWithSequenceId, serverJID(), withSequenceId));

    put();

    return errorJIDs;
  }

  public void sendDirect(String message, Member recipient) {
    sendMessage(message, Collections.singletonList(recipient));
    //TODO(someone): figure out if this is safe to do.
    if (false) {
      SendUtil.sendMessage(message,
          serverJID(),
          Collections.singletonList(new JID(recipient.getJID())));
      ChannelUtil.sendMessage(this, recipient, message);
    }
  }

  public void broadcast(String message, Member sender) {
    List<Member> recipients = getMembersToSendTo(sender);
    maybeLogMessage(message, sender, recipients);
    sendMessage(message, recipients);
  }

  private void maybeLogMessage(String message, Member sender,
      List<Member> recipients) {
    Double frac = Configuration.persistentConfig().fractionOfMessagesToLog();

    final double accept = (null == frac || frac > 1.0 || frac < 0.0) ? 
        0.0 : frac.doubleValue(); 

    if (Channel.randomGenerator.nextDouble() < accept) {
      AsyncDatastoreService asyncDS = DatastoreServiceFactory.getAsyncDatastoreService();
      Entity logEntity = createMessageLogEntity(message, sender, recipients);
      asyncDS.put(logEntity);
    }
  }

  private Entity createMessageLogEntity(String message, Member sender,
      List<Member> recipients) {
    Entity logEntity = new Entity("messageLog");
    logEntity.setUnindexedProperty("from", 
        (null == sender) ? "unknown@unknown" : sender.getJID());
    logEntity.setUnindexedProperty("to", this.getName());
    logEntity.setUnindexedProperty("num_recipients", recipients.size());
    logEntity.setUnindexedProperty("payload_size", message.length());		
    logEntity.setUnindexedProperty("time_ms", System.currentTimeMillis());
    return logEntity;
  }

  public void broadcastIncludingSender(String message) {
    List<Member> dest = getMembersToSendTo();
    maybeLogMessage(message, null, dest);
    sendMessage(message, dest);
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
      User memberUser = Datastore.instance().getUserByJID(m.getJID());
      if (memberUser.canReceiveSMS()) {
        addresses.add(
            memberUser.carrier().emailAddress(memberUser.phoneNumber()));
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

  private void incrementSequenceId() {
    ++sequenceId;
    if (sequenceId >= 100) {
      sequenceId = 0;
    }
  }

  public int getSequenceId() {
    return sequenceId;
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
    if (loggingDisabled == null) {
      // Default large rooms to disabled logging, so that their Channel entities
      // are smaller.
      loggingDisabled = members.size() > LARGE_CHANNEL_THRESHOLD;
      shouldPut = true;
    }
    if (invitedIds == null) {
      invitedIds = Lists.newArrayList();
      shouldPut = true;
    }
    if (requestedInvitations == null) {
      requestedInvitations = Lists.newArrayList();
      shouldPut = true;
    }

    List<String> membersToRemove = Lists.newArrayList();

    for (Member m : mutableMembers()) {
      // Don't allow channels to be in other channels {@link InviteHandler#
      // parseEmailAddresses} should be forbidding this, but just in case,
      // also fix this at read time.
      if (m.getJID().endsWith(Configuration.chatDomain) ||
          m.getJID().endsWith(Configuration.mailDomain)) {
        logger.warning(
            "Remove " + m.getJID() + " from " + name + " since it's a " +
            "possible infinite loop");
        membersToRemove.add(m.getJID());
        continue;
      }
      String jid = m.getJID().toLowerCase();
      if (invitedIds.contains(jid)) {
        invitedIds.remove(jid);
      }
      if (m.fixUp(this)) {
        shouldPut = true;
      }
    }

    if (!membersToRemove.isEmpty()) {
      for (String jid : membersToRemove) {
        User user = Datastore.instance().getUserByJID(jid);
        if (user != null) {
          removeMember(user);
        } else {
          // If we can't find a matching User, we should still remove the
          // member from the channel
          logger.warning("Could not find a User object for " + jid);
          Member memberToRemove = getMemberByJID(jid);
          mutableMembers().remove(memberToRemove);
          JDOHelper.makeDirty(this, "members");
        }
      }
      shouldPut = true;
    }

    if (shouldPut) {
      logger.warning("Channel " + name + " needed fixing up");
      put();
    }
  }
}