package com.imjasonh.partychapp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.Set;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;


import com.google.appengine.api.memcache.stdimpl.GCacheFactory;
import com.google.appengine.api.xmpp.JID;
import com.google.appengine.repackaged.com.google.common.collect.Maps;
import com.google.appengine.repackaged.com.google.common.collect.Sets;
import com.imjasonh.partychapp.Member.SnoozeStatus;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Channel implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 3860339764413214817L;

  // private static final Logger LOG = Logger.getLogger(Channel.class.getName());

  private static final PersistenceManager PERSISTENCE_MANAGER =
      JDOHelper.getPersistenceManagerFactory("transactions-optional").getPersistenceManager();

  private static Cache CACHE;
  static {
    try {
      CACHE = CacheManager.getInstance().getCacheFactory().createCache(
          Maps.immutableMap(GCacheFactory.EXPIRATION_DELTA, 600)); // expire after an ten mins
    } catch (CacheException ex) {
      // TODO what should happen here?
    }
  }

  @PrimaryKey
  @Persistent
  private String name;

  @Persistent(serialized = "true")
  private Set<Member> members;

  public Channel(String name) {
    this.name = name;
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

  /**
   * @param exclude
   *          a JID to exclude (for example the person sending the broadcast message)
   * @return an array of JIDs to send a message to, excluding snoozing members.
   */
  public JID[] getMembersJIDsToSendTo(JID exclude) {
    String excludeJID = exclude.getId().split("/")[0];
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

  @SuppressWarnings("unchecked")
  public static Channel getByName(String name) {
    if (CACHE.containsKey(name)) {
      return (Channel) CACHE.get(name);
    }
    try {
      Channel channel = PERSISTENCE_MANAGER.getObjectById(Channel.class, name);
      CACHE.put(name, channel);
      return channel;
    } catch (JDOObjectNotFoundException notFound) {
      return null;
    }
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

  @SuppressWarnings("unchecked")
  public void put() {
    PERSISTENCE_MANAGER.makePersistent(this);
    CACHE.put(this.name, this);
  }

  public void delete() {
    CACHE.remove(this.name);
    PERSISTENCE_MANAGER.deletePersistent(this);
  }
}
