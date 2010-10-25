// Copyright 2010 Google Inc. All Rights Reserved.

package com.imjasonh.partychapp;

import com.google.common.collect.Maps;


import java.util.Map;

/**
 * Implementation of {@link CachingDatastore} that uses an in-memory
 * {@code HashMap} for the cache. Meant for test use only.
 *
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class InMemoryCachingDatastore extends CachingDatastore {
  private Map<String, Object> cache = Maps.newHashMap();

  public InMemoryCachingDatastore(Datastore wrapped) {
    super(wrapped);
  }
  
  @Override public void addToCache(String key, Object o) {
    cache.put(key, o);
  }

  @Override public Object getFromCache(String key) {
    return cache.get(key);
  }

  @Override public void invalidateCache(String key) {
    cache.remove(key);
  }    
}