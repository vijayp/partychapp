package com.imjasonh.partychapp;

import com.google.appengine.api.memcache.jsr107cache.GCacheFactory;
import com.google.appengine.api.utils.SystemProperty;
import com.google.common.collect.ImmutableMap;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheManager;
import net.sf.jsr107cache.CacheStatistics;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of {@link CachingDatastore} that has a cache backed by 
 * the App Engine memcache service. All exceptions are logged and the cache
 * methods become no-ops, so that failures should generally be transparent.
 * Cacheable objects must implement {@link Serializable}. All keys are scoped
 * by the application version, so that pushing a new version with possibly
 * new object fields should not return in serialization issues.
 *
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class MemcacheCachingDatastore extends CachingDatastore {
  private static final Logger logger =
    Logger.getLogger(MemcacheCachingDatastore.class.getName());

  private static final long EXPIRATION_SEC = 60 * 60L;
  
  private static Cache cache = null;
  private static String applicationVersion;
  static {
    applicationVersion = SystemProperty.applicationVersion.get();
    try {
      cache = CacheManager.getInstance().getCacheFactory().createCache(
          ImmutableMap.of(GCacheFactory.EXPIRATION_DELTA, EXPIRATION_SEC));
    } catch (CacheException err) {
      logger.warning("Could not initialize MemcacheCachingDatastore cache");
    }
  }
  
  public static CacheStatistics getCacheStatistics() {
    if (cache == null) return null;
    return cache.getCacheStatistics();
  }
  
  public MemcacheCachingDatastore(Datastore wrapped) {
    super(wrapped);
  }

  @Override protected String getKey(Class<?> cls, String id) {
    return applicationVersion + "." + super.getKey(cls, id);
  }    
  
  @Override public void addToCache(String key, Object o) {
    if (cache == null) return;
    
    if (!(o instanceof Serializable)) {
      logger.warning(
          key + " does not implement Serializable, cannot be cached");
      return;
    }
    
    if (o == null) {
      logger.warning("Not caching null values for " + key);
      return;
    }
    
    try {
      cache.put(key, o);
    } catch (RuntimeException err) {
      logger.log(Level.SEVERE, "Could add " + key + " to cache", err);
    }
  }

  @Override public Object getFromCache(String key) {
    if (cache == null) return null;
    try {
      return cache.get(key);
    } catch (RuntimeException err) {
      logger.log(Level.SEVERE, "Could get" + key + " from cache", err);
      return null;
    }
  }

  @Override public void invalidateCache(String key) {
    if (cache == null) return;
    try {
      cache.remove(key);
    } catch (RuntimeException err) {
      logger.log(Level.SEVERE, "Could invalidate cache for " + key, err);
    }
  }

}
