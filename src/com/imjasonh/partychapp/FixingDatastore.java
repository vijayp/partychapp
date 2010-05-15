package com.imjasonh.partychapp;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.imjasonh.partychapp.ppb.Reason;
import com.imjasonh.partychapp.ppb.Target;


public class FixingDatastore extends Datastore {
  private final Datastore wrapped;

  public FixingDatastore(Datastore wrapped) {
    this.wrapped = wrapped;
  }
  
  @Override
  public void delete(Serializable s) {
    wrapped.delete(s);
  }

  @Override
  public void endRequest() {
    wrapped.endRequest();
  }

  @Override
  public Channel getChannelByName(String name) {
    Channel c = wrapped.getChannelByName(name);
    if (c != null) {
      c.fixUp();
    }
    return c;
  }
  
  @Override
  public User getUserByJID(String jid) {
    return wrapped.getUserByJID(jid);
  }

  @Override
  public User getUserByPhoneNumber(String phoneNumber) {
    return wrapped.getUserByPhoneNumber(phoneNumber);
  }
  
  @Override
  public Target getOrCreateTarget(Channel channel, String name) {
    Target t = wrapped.getOrCreateTarget(channel, name);
    return t;
  }
  
  @Override
  public List<Target> getTargetsByChannel(String channel) {
	  return wrapped.getTargetsByChannel(channel);
  }

  @Override
  public List<Reason> getReasons(Target target, int limit) {
    List<Reason> reasons = wrapped.getReasons(target, limit);
    if (limit <= 0) {
      target.fixUp(reasons);
    }
    return reasons;
  }

  @Override
  public Target getTargetByID(String key) {
    return wrapped.getTargetByID(key);
  }
  
  @Override
  public Datastore.Stats getStats(boolean useCache) {
    return wrapped.getStats(useCache);
  }
  
  @Override
  public void put(Serializable s) {
    wrapped.put(s);
  }

  @Override
  public void putAll(Collection<? extends Serializable> objects) {
    wrapped.putAll(objects);
  }

  @Override
  public void startRequest() {
    wrapped.startRequest();
  }
  
  @Override
  public Iterator<String> getAllChannelKeys(String lastKey) {
    return wrapped.getAllChannelKeys(lastKey);
  }
}
