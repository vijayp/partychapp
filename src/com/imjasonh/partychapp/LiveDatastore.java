package com.imjasonh.partychapp;

import java.io.Serializable;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import com.google.appengine.api.memcache.stdimpl.GCacheFactory;
import com.google.appengine.repackaged.com.google.common.collect.Maps;
import com.imjasonh.partychapp.ppb.Target;

public class LiveDatastore extends Datastore {

  private static final PersistenceManager PERSISTENCE_MANAGER = JDOHelper
          .getPersistenceManagerFactory("transactions-optional")
          .getPersistenceManager();

  private static Cache CACHE;
  static {
    try {
      // expire after 10 minutes
      CACHE = CacheManager.getInstance().getCacheFactory().createCache(
              Maps.immutableMap(GCacheFactory.EXPIRATION_DELTA, 600));
    } catch (CacheException ex) {
      // TODO what should happen here?
    }
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public Channel getByName(String name) {
    if (CACHE.containsKey(name)) {
      return (Channel)CACHE.get(name);
    }
    try {
      Channel channel = PERSISTENCE_MANAGER.getObjectById(Channel.class, name);
      CACHE.put(name, channel);
      return channel;
    } catch (JDOObjectNotFoundException notFound) {
      return null;
    }
  }
  
  @Override
  public Target getOrCreateTarget(Channel channel, String name) {
    return new Target(name, channel);
  }

  @Override
  public void put(Serializable s) {
    PERSISTENCE_MANAGER.makePersistent(s);
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public void put(Serializable s, String name) {
    PERSISTENCE_MANAGER.makePersistent(s);
    CACHE.put(name, s);
  }

  @Override
  public void delete(Serializable s) {
    PERSISTENCE_MANAGER.deletePersistent(s);
  }
  
  @Override
  public void delete(Serializable s, String name) {
    CACHE.remove(name);
    PERSISTENCE_MANAGER.deletePersistent(s);
  }
}
