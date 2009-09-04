package com.imjasonh.partychapp;

import java.io.Serializable;
import java.util.List;
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
import com.google.appengine.repackaged.com.google.common.collect.Lists;
import com.google.appengine.repackaged.com.google.common.collect.Maps;

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

  @Persistent
  private List<String> memberJIDs;

  public Channel(String name) {
    this.name = name;
    memberJIDs = Lists.newArrayList();
  }

  public void addMember(JID jid) {
    String jidString = jid.getId();
    if (!memberJIDs.contains(jidString)) {
      memberJIDs.add(jidString);
    }
  }

  public void removeMember(JID jid) {
    memberJIDs.remove(jid.getId());
    if (memberJIDs.isEmpty()) {
      memberJIDs = Lists.newArrayList();
    }
  }

  public boolean isMember(JID jid) {
    if (memberJIDs == null) {
      memberJIDs = Lists.newArrayList();
    }
    return memberJIDs.contains(jid.getId());
  }

  public boolean isEmpty() {
    return memberJIDs.isEmpty();
  }

  public JID[] getAllMemberJIDsArray() {
    JID[] jids = new JID[memberJIDs.size()];
    int i = 0;
    for (String jid : memberJIDs) {
      jids[i] = new JID(jid);
      i++;
    }
    return jids;
  }

  public JID[] getAllMemberJIDsArrayExcept(JID except, JID... excepts) {
    List<String> copy = Lists.newArrayList(memberJIDs);
    copy.remove(except.getId());
    for (JID jid : excepts) {
      copy.remove(jid.getId());
    }
    JID[] jids = new JID[copy.size()];
    for (int i = 0; i < copy.size(); i++) {
      jids[i] = new JID(copy.get(i));
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
