package com.imjasonh.partychapp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.xmpp.JID;
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

  public Channel(JID serverJID) {
    this.name = serverJID.getId().split("@")[0];
    members = Sets.newHashSet();
  }

  /**
   * Adds a member to the channel. This may alter the member's alias by prepending a _ if the
   * channel already has a member with that alias.
   * 
   * @param member
   */
  public void addMember(Member member) {
    for (Member m : members) {
      if (m.getAlias().equals(member.getAlias())) {
        member.setAlias("_" + member.getAlias());
        addMember(member);
        return;
      }
    }
    members.add(member);
  }

  public void removeMember(Member member) {
    members.remove(member);
    if (members.isEmpty()) {
      // App Engine seems to make empty sets null instead of empty sets?
      members = Sets.newHashSet();
    }
  }

  public Set<Member> awakenSnoozers() {
    Set<Member> awoken = Sets.newHashSet();
    for (Member member : members) {
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
    ArrayList<JID> jids = new ArrayList<JID>();
    for (Member member : members) {
      if (!member.getJID().equals(excludeJID)
          && member.getSnoozeStatus() != SnoozeStatus.SNOOZING) {
        jids.add(new JID(member.getJID()));
      }
    }

    JID returnJids[] = new JID[jids.size()];
    jids.toArray(returnJids);
    return returnJids;
  }

  public String getName() {
    return name;
  }

  public Set<Member> getMembers() {
    return members;
  }

  public Member getMemberByJID(JID jid) {
    String shortJID = jid.getId().split("/")[0];
    if (members == null) {
      members = Sets.newHashSet();
      return null;
    }
    for (Member member : members) {
      if (member.getJID().equals(shortJID)) {
        return member;
      }
    }
    return null;
  }

  public void put() {
    Datastore.instance().put(this, this.name);
  }

  public void delete() {
    Datastore.instance().delete(this, this.name);
  }
}
