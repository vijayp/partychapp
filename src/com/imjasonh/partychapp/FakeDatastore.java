package com.imjasonh.partychapp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.appengine.repackaged.com.google.common.base.Pair;
import com.imjasonh.partychapp.ppb.Reason;
import com.imjasonh.partychapp.ppb.Target;

public class FakeDatastore extends Datastore {
  private Map<String, Channel> channels = new HashMap<String, Channel>();
  private Map<Pair<Channel, String>, Target> targets = new HashMap<Pair<Channel, String>, Target>();
  private Map<Target, List<Reason> > reasons = new HashMap<Target, List<Reason> >();
  
  @Override
  public Channel getByName(String name) {
    return channels.get(name);
  }

  @Override
  public Target getOrCreateTarget(Channel channel, String name) {
    Pair<Channel, String> key = new Pair<Channel, String>(channel, name);
    Target t = targets.get(key);
    if (t == null) {
      t = new Target(name, channel);
      targets.put(key, t);
      reasons.put(t, new ArrayList<Reason>());
    }
    return t;
  }

  @Override
  public void delete(Serializable s, String name) {
    delete(s);
  }
  
  public void delete(Serializable s) {
    if (s instanceof Channel) {
      channels.remove(((Channel)s).getName());
    } else {
      throw new RuntimeException("delete not implemented");
    }
  }
  
  @Override
  public void put(Serializable s, String name) {
    put(s);
  }
  
  public void put(Serializable s) {
    if (s instanceof Channel) {
      Channel c = (Channel)s;
      channels.put(c.getName(), c);
    } else if (s instanceof Reason) {
      Reason r = (Reason)s;
      reasons.get(r.target()).add(r);
    } else {
      throw new RuntimeException("put not implemented");
    }
  }
}
