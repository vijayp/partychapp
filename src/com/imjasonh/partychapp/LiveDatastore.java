package com.imjasonh.partychapp;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.repackaged.com.google.common.collect.Lists;
import com.imjasonh.partychapp.ppb.Reason;
import com.imjasonh.partychapp.ppb.Target;

// NOT thread-safe
public class LiveDatastore extends Datastore {

  private static final PersistenceManagerFactory PERSISTENCE_FACTORY = JDOHelper
      .getPersistenceManagerFactory("transactions-optional");

  // Created transiently for each request.
  private PersistenceManager manager;
  
  @Override
  public Channel getChannelByName(String name) {
    Channel c = null;
    try {
      c = manager.getObjectById(Channel.class, name);
    } catch (JDOObjectNotFoundException notFound) {
      return null;
    }

    return attachUsersToChannelMembers(c);
  }
  
  @Override
  public User getUserByJID(String jid) {
    try {
      User user = manager.getObjectById(User.class, jid);
      return user;
    } catch (JDOObjectNotFoundException notFound) {
      return null;
    }    
  }

  @Override
  public User getUserByPhoneNumber(String phoneNumber) {
    Query query = manager.newQuery(User.class);
    query.setFilter("phoneNumber == phoneNumberParam");
    query.declareParameters("String phoneNumberParam");

    @SuppressWarnings("unchecked")
    List<User> users = (List<User>) query.execute(phoneNumber);
    query.closeAll();
    if ((users != null) && !users.isEmpty()) {
      return users.get(0);
    }
    return null;
  }
  
  @Override
  public List<User> getUsersByChannel(Channel c) {
    Query query = manager.newQuery(User.class);
    query.setFilter("channelNames.contains(channelNameParam)");
    query.declareParameters("String channelNameParam");

    @SuppressWarnings("unchecked")
    List<User> users = (List<User>) query.execute(c.getName());
    query.closeAll();
    if (users == null) {
      return Lists.newArrayList();
    }
    return users;
  }

  @Override
  public Target getTargetByID(String key) {
    try {
      return manager.getObjectById(Target.class, key);
    } catch (JDOObjectNotFoundException e) {
      // TODO(nsanch): there has to be a better way
      return null;
    }
  }

  @Override
  public Target getOrCreateTarget(Channel channel, String name) {
    Target t = getTarget(channel, name);
    if (t == null) {
      t = new Target(name, channel);
    }
    return t;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public List<Target> getTargetsByChannel(String channelName) {
    Query query = manager.newQuery(Target.class);
    query.setFilter("channelName == channelNameParam");
    //	query.setOrdering("hireDate desc");
    query.declareParameters("String channelNameParam");

    try {
      return (List<Target>) query.execute(channelName);
    } finally {
      query.closeAll();
    }
  }

  public List<Reason> getReasons(Target target, int limit) {
    Query query = manager.newQuery(Reason.class);
    query.setFilter("targetId == targetIdParam");
    query.setOrdering("timestamp desc");
    query.declareParameters("String targetIdParam");
    if (limit > 0) {
      query.setRange(0, limit);
    }

    @SuppressWarnings("unchecked")
    List<Reason> reasons = (List<Reason>) query.execute(target.key());
    query.closeAll();
    return reasons;
  }

  @Override
  public void put(Serializable s) {
    manager.makePersistent(s);
  }

  @Override
  public void putAll(Collection<? extends Serializable> objects) {
    manager.makePersistentAll(objects);
  }

  @Override
  public void delete(Serializable s) {
    manager.deletePersistent(s);
  }

  @Override
  public void endRequest() {
    manager.close();
  }

  @Override
  public void startRequest() {
    manager = PERSISTENCE_FACTORY.getPersistenceManager();
  }

  @Override
  public Datastore.Stats getStats() {
    Datastore.Stats ret = new Datastore.Stats();
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery pq = datastore.prepare(new com.google.appengine.api.datastore.Query("__Stat_Kind__"));
    for (Entity kindStat : pq.asIterable()) {
      String kind = (String)kindStat.getProperty("kind_name");
      if ("Channel".equals(kind)) {
        ret.numChannels = ((Long)kindStat.getProperty("count")).intValue();
        ret.timestamp = (Date)kindStat.getProperty("timestamp");
      } else if ("User".equals(kind)) {
        ret.numUsers = ((Long)kindStat.getProperty("count")).intValue();
      }
    } 
    
    return ret;
  }
}
