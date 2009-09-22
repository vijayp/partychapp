package com.imjasonh.partychapp;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import com.imjasonh.partychapp.ppb.Reason;
import com.imjasonh.partychapp.ppb.Target;

public abstract class Datastore {
  private static Datastore instance;

  public static Datastore instance() {
    if (instance == null) {
      // We have to do this lazily because tests won't have the
      // live datastore dependencies set up
      instance = new LiveDatastore();
    }
    return instance;
  }
  
  public static void setInstance(Datastore ds) {
    instance = ds;
  }
  
  public abstract Channel getChannelByName(String name);

  public abstract Target getTarget(Channel channel, String name);
  public abstract Target getOrCreateTarget(Channel channel, String name);
  public abstract Target getTargetByID(String key);

  public abstract List<Reason> getReasons(Target target, int limit);
  
  public abstract void putAll(Collection<? extends Serializable> objects);
  public abstract void put(Serializable s);
  public abstract void delete(Serializable s);

  public abstract void startRequest();
  public abstract void endRequest();
}