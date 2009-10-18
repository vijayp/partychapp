package com.imjasonh.partychapp;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.xmpp.JID;
import com.google.appengine.repackaged.com.google.common.collect.Lists;
import com.google.appengine.repackaged.com.google.common.collect.Maps;
import com.imjasonh.partychapp.ppb.Reason;
import com.imjasonh.partychapp.ppb.Target;

public class FakeDatastore extends Datastore {
  private Map<String, Channel> channels = Maps.newHashMap();
  private Map<Key, Member> members = Maps.newHashMap();
  private Map<String, Target> targets = Maps.newHashMap();
  private Map<String, List<Reason> > reasons = Maps.newHashMap();
  
  public static FakeDatastore instance() {
    FixingDatastore fixing = (FixingDatastore)Datastore.instance();
    return (FakeDatastore)fixing.getWrapped();
  }
  
  public FakeDatastore() {
    Channel channel = new Channel(new JID("pancake@partychat.appspotchat.com"));
    // using fake addresses to avoid leaking our email addresses publicly
    channel.addMember(new JID("neil@gmail.com"));
    channel.addMember(new JID("jason@gmail.com"));
    channel.addMember(new JID("kushal@kushaldave.com"));
    channel.addMember(new JID("david@gmail.com"));
    channel.addMember(new JID("akshay@q00p.net"));
    put(channel);
  }

  @Override
  public Channel getChannelByName(String name) {
    Channel c = channels.get(name);
    if (c != null) {
      return new Channel(c);
    }
    return null;
  }
  
  @Override
  public Target getTargetByID(String key) {
    Target t = targets.get(key);
    if (t != null) {
      return new Target(t);
    }
    return null;
  }
  
  @Override
  public List<Reason> getReasons(Target target, int limit) {
    List<Reason> list = reasons.get(target.key());
    if (list == null) {
      list =  Lists.newArrayList();
    } else if ((limit > 0) && (list.size() > limit)) {
      list = list.subList(0, limit-1);
    }
    return list;
  }
  
  @Override
  public Map<String, Integer> getStats() {
    Map<String, Integer> ret = Maps.newHashMap();
    ret.put("Channel", channels.size());
    ret.put("Member", members.size());
    return ret;
  }
  
  @Override
  public void delete(Serializable s) {
    if (s instanceof Channel) {
      channels.remove(((Channel)s).getName());
    } else {
      throw new RuntimeException("delete not implemented");
    }
  }
  
  @Override
  public void putAll(Collection<? extends Serializable> objects) {
    for (Serializable s : objects) {
      put(s);
    }
  }

  @Override
  public void put(Serializable s) {
    if (s instanceof Channel) {
      Channel c = (Channel)s;
      channels.put(c.getName(), c);
    } else if (s instanceof Reason) {
      Reason r = (Reason)s;
      reasons.get(r.target().key()).add(0, r);
    } else if (s instanceof Target) {
      Target t = (Target)s;
      targets.put(t.key(), t);
      if (!reasons.containsKey(t.key())) {
        reasons.put(t.key(), Lists.<Reason>newArrayList());
      }
    } else if (s instanceof Member) {
      Member m = (Member)s;
      members.put(m.key(), m);
    } else {
      throw new RuntimeException("put not implemented for " + s);
    }
  }

  public Channel fakeChannel() {
    return getChannelByName("pancake");
  }

  public void clear() {
    channels = Maps.newHashMap();
    targets = Maps.newHashMap();
    reasons = Maps.newHashMap();
  }

  @Override
  public void endRequest() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void startRequest() {
    // TODO Auto-generated method stub

  }
}