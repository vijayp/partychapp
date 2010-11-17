package com.imjasonh.partychapp;

import com.google.appengine.repackaged.com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.imjasonh.partychapp.ppb.Reason;
import com.imjasonh.partychapp.ppb.Target;
import com.imjasonh.partychapp.server.admin.ChannelInvalidateServlet;
import com.imjasonh.partychapp.server.admin.ChannelServlet;
import com.imjasonh.partychapp.server.admin.UserServlet;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Wrapper around {@link Datastore} that adds caching for {@link User} and
 * {@link Channel} instances (actual implementation of caching is left to
 * subclasses).
 * 
 * A certain degree of write-back caching is implemented. Writes (to either
 * the cache or the datastore) are not done immediately when put() or putAll()
 * are called. Instead, they are buffered (in a ThreadLocal request cache)
 * until endRequest() is called and only then persisted. This avoids having to
 * commit the same entity more than once (e.g. for most requests we call
 * Channel.put() twice, once when we update the sequence ID and once when we add
 * the a member's recent messages to that list). Calls to various get* methods
 * after a put() are served from this request cache.
 *
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public abstract class CachingDatastore extends WrappingDatastore {
  
  private final ThreadLocal<Map<String, Object>> requestCache =
      new ThreadLocal<Map<String, Object>>() {
    @Override protected Map<String, Object> initialValue() {
      return Maps.newHashMap();
    }
  };

  protected CachingDatastore(Datastore wrapped) {
    super(wrapped);
  }
  
  protected abstract void invalidateCache(String key);
  protected abstract void addToCache(String key, Object o);
  protected abstract Object getFromCache(String key);
  
  protected String getKey(Class<?> cls, String id) {
    return cls.getCanonicalName() + "#" + id;
  }
  
  private boolean addToRequestCacheIfNecessary(Object o) {
    String key = null;
    if (o instanceof Channel) {
      key = getKey((Channel) o);
    } else if (o instanceof User) {
      key = getKey((User) o);
    }
    
    if (key != null) {
      Map<String, Object> cache = requestCache.get();
      cache.put(key, o);
      return true;
    }
    
    return false;
  }
  
  private Object getFromRequestCacheOrCache(String key) {
    Map<String, Object> cache = requestCache.get();
    
    Object value = cache.get(key);
    if (value != null) {
      return value;
    }
   
    return getFromCache(key);
  }

  /**
   * Should be private, but is also used by {@link ChannelServlet}.
   */
  public String getKey(Channel channel) {
    return getKey(Channel.class, channel.getName());
  }
  
  /**
   * Should be private, but is also used by {@link UserServlet}.
   */  
  public String getKey(User user) {
    return getKey(User.class, user.getJID());
  }
  
  /**
   * Should be private, but is also used by {@link ChannelInvalidateServlet}.
   */
  public void invalidateCacheIfNecessary(Object o) {
    if (o instanceof Channel) {
      invalidateCache(getKey((Channel) o));
    } else if (o instanceof User) {
      invalidateCache(getKey((User) o));
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
    Channel channel = (Channel) getFromRequestCacheOrCache(key);
    if (channel == null) {
      channel = wrapped.getChannelByName(name);
      if (channel != null) {
        addToCache(key, channel);
      }
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
    User user = (User) getFromRequestCacheOrCache(key);
    if (user == null) {
      user = wrapped.getUserByJID(jid);
      if (user != null) {
        addToCache(key, user);
      }
    }
    return user;    
  }

  @Override public User getUserByPhoneNumber(String phoneNumber) {
    return wrapped.getUserByPhoneNumber(phoneNumber);
  }

  @Override public void put(Object o) {
    if (!addToRequestCacheIfNecessary(o)) {
      wrapped.put(o);
    }
  }

  @Override public void putAll(Collection<Object> objects) {
    List<Object> notInRequestCache =
        Lists.newArrayListWithExpectedSize(objects.size());
    for (Object o : objects) {
      if (!addToRequestCacheIfNecessary(o)) {
        notInRequestCache.add(o);
      }
    }
    
    if (!notInRequestCache.isEmpty()) {
      wrapped.putAll(notInRequestCache);
    }
  }

  @Override public void startRequest() {
    wrapped.startRequest();
    assert requestCache.get().isEmpty();
    requestCache.get().clear();
  }

  @Override public void endRequest() {
    Map<String, Object> cache = requestCache.get();
    for (Map.Entry<String, Object> entry : cache.entrySet()) {
      addToCache(entry.getKey(), entry.getValue());
    }
    wrapped.putAll(cache.values());
    requestCache.get().clear();
    wrapped.endRequest();
  }
}
