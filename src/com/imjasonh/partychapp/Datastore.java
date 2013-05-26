package com.imjasonh.partychapp;

import com.google.appengine.api.xmpp.JID;
import com.google.common.annotations.VisibleForTesting;

import com.imjasonh.partychapp.ppb.Reason;
import com.imjasonh.partychapp.ppb.Target;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Logger;

public abstract class Datastore {
  private static Datastore instance;
  
  @SuppressWarnings("unused")
  private static final Logger logger = 
      Logger.getLogger(Datastore.class.getName());

  public static Datastore instance() {
    if (instance == null) {
      // We have to do this lazily because tests won't have the
      // live datastore dependencies set up
      instance = new FixingDatastore(new MemcacheCachingDatastore(
          new LiveDatastore()));
    }
    return instance;
  }
  

  @VisibleForTesting public static void setInstance(Datastore ds) {
    instance = new FixingDatastore(ds);
  }
  
  public abstract PersistentConfiguration getPersistentConfig();
  
  public abstract Channel getChannelByName(String name);
  
  public abstract User getUserByJID(String jid);
  public abstract User getUserByPhoneNumber(String phoneNumber);
  public class NotImplementedException extends Exception {}
  
  public Iterable<Channel> getChannelsByMigrationStatus(
        boolean migrated) throws NotImplementedException {
    throw (new NotImplementedException());
  }

  
  public List<Channel> getChannelsForGmailUsername(
      String gmailUserName) throws NotImplementedException {
	    throw (new NotImplementedException());
  }

  
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

  public static class Stats implements Serializable {
    public int numChannels;
    public int numUsers;
    public Date timestamp;
    public int oneDayActiveUsers;
    public int sevenDayActiveUsers;
    public int thirtyDayActiveUsers;

    private static final DateFormat dateFormat =
        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
    static {
      dateFormat.setTimeZone(TimeZone.getTimeZone("America/New_York"));
    }
    
    private static final NumberFormat numberFormat =
        NumberFormat.getIntegerInstance(Locale.US);
    
    public String getFormattedNumChannels() {
      return numberFormat.format(numChannels); 
    }
    
    public String getFormattedSevenDayActiveUsers() {
      return numberFormat.format(sevenDayActiveUsers); 
    }    
    
    @Override
    public String toString() {
      String reply = "Number of channels (as of " + 
          (timestamp != null ? dateFormat.format(timestamp) : "unknown") + 
          "): " + numChannels + "\n";
      reply += "1-day active users: " + oneDayActiveUsers + "\n";
      reply += "7-day active users: " + sevenDayActiveUsers + "\n";
      reply += "30-day active users: " + thirtyDayActiveUsers + "\n";
      reply += "Number of users: " + numUsers + "\n";
      return reply;
    }
  }
  
  public abstract Stats getStats(boolean useCache);
  
  public abstract void putAll(Collection<Object> objects);
  public abstract void put(Object o);
  public abstract void delete(Object o);

  public abstract void startRequest();
  public abstract void endRequest();
  
  public Channel getChannelIfUserPresent(String channelName, String email) {
    Channel channel = getChannelByName(channelName);
    if (channel == null) {
      return null;
    }

    if (channel.getMemberByJID(new JID(email)) == null) {
      return null;
    }
    return channel;
  }
  
  
  public abstract Iterator<String> getAllEntityKeys(
      Class<?> entityClass, String lastKey);
}