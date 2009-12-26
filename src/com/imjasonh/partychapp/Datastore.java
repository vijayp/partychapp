package com.imjasonh.partychapp;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.xmpp.JID;
import com.imjasonh.partychapp.ppb.Reason;
import com.imjasonh.partychapp.ppb.Target;

public abstract class Datastore {
  private static Datastore instance;
  
  @SuppressWarnings("unused")
  private static final Logger LOG = Logger.getLogger(Datastore.class.getName());

  public static Datastore instance() {
    if (instance == null) {
      // We have to do this lazily because tests won't have the
      // live datastore dependencies set up
      instance = new FixingDatastore(new LiveDatastore());
    }
    return instance;
  }
  
  public static void setInstance(Datastore ds) {
    instance = new FixingDatastore(ds);
  }
  
  public abstract Channel getChannelByName(String name);
  
  public Channel attachUsersToChannelMembers(Channel c) {
    List<User> users = getUsersByChannel(c);
    for (Member m : c.getMembers()) {
      if (m.user() != null) {
        continue;
      }
      for (User u : users) {
        if (u.getJID().equals(m.getJID())) {
          m.setUser(u);
          break;
        }
      }
      if (m.user() == null) {
        User u = getOrCreateUser(m.getJID());
        m.setUser(u);
        m.user().addChannel(c.getName());
        m.user().put();
      }
    }
    return c;
  }

  public abstract User getUserByJID(String jid);
  public abstract User getUserByPhoneNumber(String phoneNumber);
  public abstract List<User> getUsersByChannel(Channel c);
  
  public User getOrCreateUser(String jid) {
    User u = getUserByJID(jid);
    if (u != null) {
      return u;
    }
    return new User(jid);
  }
  
  public abstract Target getTargetByID(String key);
  
  public Target getTarget(Channel channel, String name) {
    return getTargetByID(Target.createTargetKey(name, channel));
  }

  public Target getOrCreateTarget(Channel channel, String name) {
    Target t = getTarget(channel, name);
    if (t == null) {
      t = new Target(name, channel);
    }
    return t;
  }
  
  public List<Target> getTargetsByChannel(Channel channel) {
	  return getTargetsByChannel(channel.getName());
  }
  
  public abstract List<Target> getTargetsByChannel(String channel);

  public abstract List<Reason> getReasons(Target target, int limit);

  public static class Stats {
    public int numChannels;
    public int numUsers;
    public Date timestamp;
    public int oneDayActiveUsers;
    public int sevenDayActiveUsers;
    public int thirtyDayActiveUsers;

    private static final DateFormat df =
      DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
    
    public String toString() {
      String reply = "Number of channels (as of " + df.format(timestamp) + "): " + numChannels + "\n";
      reply += "1-day active users: " + oneDayActiveUsers + "\n";
      reply += "7-day active users: " + sevenDayActiveUsers + "\n";
      reply += "Number of users: " + numUsers + "\n";
      // TODO(nsanch): uncomment when we've had the User object for more than
      // 30 days (mid-January)
      // reply += "30-day active users: " + stats.thirtyDayActiveUsers + "\n";
      return reply;
    }
  }
  public abstract Stats getStats();
  
  public abstract void putAll(Collection<? extends Serializable> objects);
  public abstract void put(Serializable s);
  public abstract void delete(Serializable s);

  public abstract void startRequest();
  public abstract void endRequest();
  
  public Channel getChannelIfUserPresent(String channelName, String email) throws IOException {
	  Channel channel = getChannelByName(channelName);
	  if (channel == null) {
		  System.out.println("Sorry room name is not there: " + channelName);
		  return null;
	  } 

	  if (channel.getMemberByJID(new JID(email)) == null) {
		  System.out.println("Sorry you're not in that room: " + email);
		  return null;
	  }
	  return channel;
  }
  
  
  public abstract Iterator<String> getAllChannelKeys(String lastKey);
}