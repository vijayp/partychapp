package com.imjasonh.partychapp.testing;

import com.google.appengine.api.xmpp.JID;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.User;
import com.imjasonh.partychapp.ppb.Reason;
import com.imjasonh.partychapp.ppb.Target;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FakeDatastore extends Datastore {
  private Map<String, Channel> channels = Maps.newHashMap();
  private Map<String, Target> targets = Maps.newHashMap();
  private Map<String, List<Reason> > reasons = Maps.newHashMap();
  private Map<String, User> users = Maps.newHashMap();
  
  public FakeDatastore() {
  }
  
  public void setUp() {
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
    if (c == null) {
      return null;
    }
    return attachUsersToChannelMembers(new Channel(c));
  }
  
  @Override
  public boolean isJIDInChannel(String channelName, String jid) {
    Channel c = channels.get(channelName);
    if (c == null) {
      return false;
    }
    
    return c.getMemberByJID(new JID(jid)) != null;
  }    
  
  @Override
  public User getUserByJID(String jid) {
    User u = users.get(jid);
    if (u != null) {
      return new User(u);
    }
    return null;
  }

  @Override
  public User getUserByPhoneNumber(String phoneNumber) {
    for (User u : users.values()) {
      if (u.phoneNumber().equals(phoneNumber)) {
        return new User(u);
      }
    }
    return null;
  }

  @Override
  public List<User> getUsersByChannel(Channel c) {
    List<User> ret = Lists.newArrayList();
    for (User u : users.values()) {
      if (u.channelNames().contains(c.getName())) {
        ret.add(u);
      }
    }
    return ret;
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
  public List<Target> getTargetsByChannel(String channel) {
	  return new ArrayList<Target>(targets.values());
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
  public Datastore.Stats getStats() {
    Datastore.Stats ret = new Datastore.Stats();
    ret.numChannels = channels.size();
    ret.numUsers = users.size();
    ret.timestamp = new Date(1256134870830L);

    ret.oneDayActiveUsers = 1;
    ret.sevenDayActiveUsers = 2;
    ret.thirtyDayActiveUsers = 3;

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
      throw new RuntimeException(
          "put() should never be called on Member, it is persisted via " +
          "serialization inside of Channel");
    } else if (s instanceof User) {
      User u = (User)s;
      users.put(u.getJID(), u);
    } else {
      throw new RuntimeException("put not implemented for " + s);
    }
  }

  public static Channel fakeChannel() {
    return Datastore.instance().getChannelByName("pancake");
  }

  @Override
  public void endRequest() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void startRequest() {
    // TODO Auto-generated method stub

  }
  
  @Override  
  public Iterator<String> getAllChannelKeys(String lastKey) {
    List<String> ret = Lists.newArrayList();
    for (String s : channels.keySet()) {
      if ((lastKey == null) || s.compareTo(lastKey) > 0) {
        ret.add(s);
      }
    }
    Collections.sort(ret);
    return ret.iterator();
  }
}