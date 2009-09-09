package com.imjasonh.partychapp;

import java.io.Serializable;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

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

  private static final Logger LOG = Logger.getLogger(Channel.class.getName());

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

  public Member addMember(JID jid) {
    if (getMemberByJID(jid) == null) {
      Member member = new Member(jid);
      members.add(member);
      return member;
    }
    return null;
  }

  public Member removeMember(JID jid) {
    Member member = getMemberByJID(jid);
    if (member != null) {
      members.remove(member);
    }
    if (members.isEmpty()) {
      // App Engine seems to make empty sets null instead of empty sets.
      members = Sets.newHashSet();
    }
    return member;
  }

  public boolean isMember(JID jid) {
    return getMemberByJID(jid) != null;
  }

  public boolean isEmpty() {
    return members.isEmpty();
  }

  /**
   * @param exclude
   *          a JID to exclude (for example the person sending the broadcast message)
   * @return an array of JIDs to send a message to, excluding snoozing members.
   */
  public JID[] getMembersJIDsToSendTo(JID exclude) {
    String excludeJID = exclude.getId().split("/")[0];
    JID[] jids = new JID[members.size()];
    int i = 0;
    boolean needToPut = false; // whether or not the channel needs to update
    for (Member member : members) {
      if (member.getJID().equals(excludeJID)) {
        continue;
      }
      SnoozeStatus snoozeStatus = member.getSnoozeStatus();
      switch (snoozeStatus) {
        case SNOOZING:
          continue; // skip this one
        case NOT_SNOOZING:
          break; // add this one
        case SHOULD_WAKE:
          member.setSnoozeUntil(null);
          needToPut = true;
          break;
        default:
          LOG.log(Level.WARNING, "Invalid SnoozeStatus " + snoozeStatus);
      }
      jids[i++] = new JID(member.getJID());
    }
    if (needToPut) {
      put();
    }
    return jids;
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
    // CACHE.remove(this.name);
    PERSISTENCE_MANAGER.deletePersistent(this);
  }
}
