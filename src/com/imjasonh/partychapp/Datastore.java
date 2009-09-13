package com.imjasonh.partychapp;

import java.io.Serializable;

import com.imjasonh.partychapp.ppb.Target;

public abstract class Datastore {
  private static Datastore instance;

  public static Datastore get() {
    if (instance == null) {
      instance = new LiveDatastore();
    }
    return instance;
  }
  
  public static void register(Datastore ds) {
    instance = ds;
  }
  
  public abstract Channel getByName(String name);
  public abstract Target getOrCreateTarget(Channel channel, String name);

  public abstract void put(Serializable s);
  public abstract void put(Serializable s, String name);
  public abstract void delete(Serializable s);
  public abstract void delete(Serializable s, String name);
}