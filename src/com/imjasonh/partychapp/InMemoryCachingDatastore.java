// Copyright 2010 Google Inc. All Rights Reserved.

package com.imjasonh.partychapp;

import com.google.common.collect.Maps;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

/**
 * Implementation of {@link CachingDatastore} that uses an in-memory
 * {@code HashMap} for the cache. Meant for test use only.
 *
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class InMemoryCachingDatastore extends CachingDatastore {
  private Map<String, byte[]> cache = Maps.newHashMap();

  public InMemoryCachingDatastore(Datastore wrapped) {
    super(wrapped);
  }
  
  @Override public void addToCache(String key, Object o) {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try {
      ObjectOutputStream out = new ObjectOutputStream(bytes);
      out.writeObject(o);
    } catch (IOException err) {
      throw new RuntimeException(err);
    }
    cache.put(key, bytes.toByteArray());
  }

  @Override public Object getFromCache(String key) {
    byte[] bytes = cache.get(key);
    if (bytes == null) {
      return null;
    }
    try {
      ObjectInputStream in =
            new ObjectInputStream(new ByteArrayInputStream(bytes));
      return in.readObject();
    } catch (IOException err) {
      throw new RuntimeException(err);      
    } catch (ClassNotFoundException err) {
      throw new RuntimeException(err);      
    }
  }

  @Override public void invalidateCache(String key) {
    cache.remove(key);
  }    
}