package com.imjasonh.partychapp.testing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.appengine.api.xmpp.JID;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.PersistentConfiguration;
import com.imjasonh.partychapp.User;
import com.imjasonh.partychapp.ppb.Reason;
import com.imjasonh.partychapp.ppb.Target;

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
    channel.addMember(getOrCreateUser("neil@gmail.com"));
    channel.addMember(getOrCreateUser("jason@gmail.com"));
    channel.addMember(getOrCreateUser("kushal@kushaldave.com"));
    channel.addMember(getOrCreateUser("david@gmail.com"));
    channel.addMember(getOrCreateUser("akshay@q00p.net"));
    put(channel);
  }

  @Override
  public PersistentConfiguration getPersistentConfig() {
    return null;
  }

  @Override
  public Channel getChannelByName(String name) {
    return channels.get(name);
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
    
    for (Reason reason : list) {
      reason.clearSender();
    }
    
    return list;
  }
  
  @Override
  public Datastore.Stats getStats(boolean useCache) {
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
  public void delete(Object o) {
    if (o instanceof Channel) {
      channels.remove(((Channel) o).getName());
    } if (o instanceof User) {
      users.remove(((User) o).getJID());
    } else {
      throw new RuntimeException("delete not implemented");
    }
  }
  
  @Override
  public void putAll(Collection<Object> objects) {
    for (Object o : objects) {
      put(o);
    }
  }

  @Override
  public void put(Object o) {
    if (o instanceof Channel) {
      Channel c = (Channel)o;
      channels.put(c.getName(), c);
    } else if (o instanceof Reason) {
      Reason r = (Reason)o;
      reasons.get(r.target().key()).add(0, r);
    } else if (o instanceof Target) {
      Target t = (Target)o;
      targets.put(t.key(), t);
      if (!reasons.containsKey(t.key())) {
        reasons.put(t.key(), Lists.<Reason>newArrayList());
      }
    } else if (o instanceof Member) {
      throw new RuntimeException(
          "put() should never be called on Member, it is persisted via " +
          "serialization inside of Channel");
    } else if (o instanceof User) {
      User u = (User)o;
      users.put(u.getJID(), u);
    } else {
      throw new RuntimeException("put not implemented for " + o);
    }
  }

  public static Channel fakeChannel() {
    return Datastore.instance().getChannelByName("pancake");
  }

  @Override
  public void endRequest() {
    // Not necessary
  }

  @Override
  public void startRequest() {
    // Not necessary
  }
  
  @Override  
  public Iterator<String> getAllEntityKeys(
      Class<?> entityClass, String lastKey) {
    List<String> ret = Lists.newArrayList();
    Set<String> keySet;
    
    if (entityClass.equals(Channel.class)) {
      keySet = channels.keySet();
    } else if (entityClass.equals(User.class)) {
      keySet = users.keySet();
    } else if (entityClass.equals(Target.class)) {
      keySet = targets.keySet();
    } else if (entityClass.equals(Reason.class)) {
      keySet = reasons.keySet();
    } else {
      throw new RuntimeException(
          "Unexpected entity class" + entityClass.getName());
    }
    
    for (String s : keySet) {
      if ((lastKey == null) || s.compareTo(lastKey) > 0) {
        ret.add(s);
      }
    }
    Collections.sort(ret);
    return ret.iterator();
  }
}