package com.imjasonh.partychapp;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.repackaged.com.google.common.collect.Maps;
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
    try {
      Channel channel = manager.getObjectById(Channel.class, name);
      return channel;
    } catch (JDOObjectNotFoundException notFound) {
      return null;
    }
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
  public List<Reason> getReasons(Target target, int limit) {
    Query query = manager.newQuery(Reason.class);
    query.setFilter("targetId == targetIdParam");
    query.setOrdering("timestamp desc");
    query.declareParameters("String targetIdParam");
    if (limit > 0) {
      query.setRange(0, limit);
    }

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
  public Map<String, Integer> getStats() {
    Map<String, Integer> ret = Maps.newHashMap();
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery pq = datastore.prepare(new com.google.appengine.api.datastore.Query("__Stat_Kind__"));
    for (Entity kindStat : pq.asIterable()) {
      ret.put((String)kindStat.getProperty("kind_name"),
              ((Long)kindStat.getProperty("count")).intValue());
    }
    
    return ret;
  }
}
