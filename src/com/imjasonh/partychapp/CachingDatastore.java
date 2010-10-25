package com.imjasonh.partychapp;

import com.imjasonh.partychapp.ppb.Reason;
import com.imjasonh.partychapp.ppb.Target;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Wrapper around {@link Datastore} that adds caching for {@link User} and
 * {@link Channel} instances (actual implementation of caching is left to
 * subclasses).
 *
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public abstract class CachingDatastore extends Datastore {

  private final Datastore wrapped;

  protected CachingDatastore(Datastore wrapped) {
    this.wrapped = wrapped;
  }
  
  public abstract void invalidateCache(String key);
  public abstract void addToCache(String key, Object o);
  public abstract Object getFromCache(String key);
  
  protected String getKey(Class<?> cls, String id) {
    return cls.getCanonicalName() + "#" + id;
  }
  
  private void addToCacheIfNecessary(Object o) {
    if (o instanceof Channel) {
      Channel c = (Channel) o;
      addToCache(getKey(Channel.class, c.getName()), c);
    } else if (o instanceof User) {
      User u = (User) o;      
      addToCache(getKey(User.class, u.getJID()), u);
    }
  }  
  
  private void invalidateCacheIfNecessary(Object o) {
    if (o instanceof Channel) {
      Channel c = (Channel) o;
      invalidateCache(getKey(Channel.class, c.getName()));
    } else if (o instanceof User) {
      User u = (User) o;      
      invalidateCache(getKey(User.class, u.getJID()));
    }
  }
  
  @Override public void delete(Object o) {
    invalidateCacheIfNecessary(o);
    wrapped.delete(o);
  }

  @Override public Iterator<String> getAllEntityKeys(
      Class<?> entityClass, String lastKey) {
    return wrapped.getAllEntityKeys(entityClass, lastKey);
  }

  @Override public Channel getChannelByName(String name) {
    String key = getKey(Channel.class, name);
    Channel channel = (Channel) getFromCache(key);
    if (channel == null) {
      channel = wrapped.getChannelByName(name);
      addToCache(key, channel);
    }
    return channel;
  }

  @Override public PersistentConfiguration getPersistentConfig() {
    return wrapped.getPersistentConfig();
  }

  @Override public List<Reason> getReasons(Target target, int limit) {
    return wrapped.getReasons(target, limit);
  }

  @Override public Stats getStats(boolean useCache) {
    return wrapped.getStats(useCache);
  }

  @Override public Target getTargetByID(String key) {
    return wrapped.getTargetByID(key);
  }

  @Override public List<Target> getTargetsByChannel(String channel) {
    return wrapped.getTargetsByChannel(channel);
  }

  @Override public User getUserByJID(String jid) {
    String key = getKey(User.class, jid);
    User user = (User) getFromCache(key);
    if (user == null) {
      user = wrapped.getUserByJID(jid);
      addToCache(key, user);
    }
    return user;    
  }

  @Override public User getUserByPhoneNumber(String phoneNumber) {
    return wrapped.getUserByPhoneNumber(phoneNumber);
  }

  @Override public void put(Object o) {
    wrapped.put(o);
    addToCacheIfNecessary(o);
  }

  @Override public void putAll(Collection<Object> objects) {
    wrapped.putAll(objects);
    for (Object o : objects) {
      addToCacheIfNecessary(o);
    }
  }

  @Override public void startRequest() {
    wrapped.startRequest();
  }

  @Override public void endRequest() {
    wrapped.endRequest();
  }
}
