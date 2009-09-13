package com.imjasonh.partychapp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.xmpp.JID;
import com.imjasonh.partychapp.ppb.Reason;
import com.imjasonh.partychapp.ppb.Target;

public class FakeDatastore extends Datastore {
  private Map<String, Channel> channels = new HashMap<String, Channel>();
  private Map<String, Target> targets = new HashMap<String, Target>();
  private Map<Target, List<Reason> > reasons = new HashMap<Target, List<Reason> >();

  public FakeDatastore() {
    Channel channel = new Channel(new JID("pancake@partychat.appspotchat.com"));
    // using fake addresses to avoid leaking our email addresses publicly
    channel.addMember(new Member(new JID("neil@gmail.com")));
    channel.addMember(new Member(new JID("jason@gmail.com")));
    channel.addMember(new Member(new JID("kushal@kushaldave.com")));
    channel.addMember(new Member(new JID("david@gmail.com")));
    channel.addMember(new Member(new JID("akshay@q00p.net")));
    put(channel);
  }

  @Override
  public Channel getChannelByName(String name) {
    return channels.get(name);
  }
  
  public Target getTargetByID(String key) {
    return targets.get(key);
  }

  @Override
  public Target getTarget(Channel channel, String name) {
    String key = Target.createTargetKey(name, channel);
    return targets.get(key);
  }
  
  @Override
  public Target getOrCreateTarget(Channel channel, String name) {
    Target t = getTarget(channel, name);
    if (t == null) {
      t = new Target(name, channel);
    }
    return t;
  }
  
  public List<Reason> getReasons(Target target, int limit) {
    List<Reason> fullList = reasons.get(target);
    if (fullList.size() > limit) {
      return fullList.subList(0, limit-1);
    }
    return fullList;
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
  
  public void putAll(Collection<Serializable> objects) {
    for (Serializable s : objects) {
      put(s);
    }
  }
  
  @Override
  public void put(Serializable s, String name) {
    put(s);
  }
  
  @Override
  public void put(Serializable s) {
    if (s instanceof Channel) {
      Channel c = (Channel)s;
      channels.put(c.getName(), c);
    } else if (s instanceof Reason) {
      Reason r = (Reason)s;
      reasons.get(r.target()).add(r);
    } else if (s instanceof Target) {
      Target t = (Target)s;
      targets.put(t.key(), t);
      if (!reasons.containsKey(t)) {
        reasons.put(t, new ArrayList<Reason>());
      }
    } else {
      throw new RuntimeException("put not implemented for " + s);
    }
  }
}
