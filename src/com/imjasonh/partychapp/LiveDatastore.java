package com.imjasonh.partychapp;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.memcache.InvalidValueException;
import com.google.appengine.api.memcache.jsr107cache.GCacheFactory;
import com.google.common.collect.ImmutableMap;

import com.imjasonh.partychapp.ppb.Reason;
import com.imjasonh.partychapp.ppb.Target;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheManager;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

// NOT thread-safe
public class LiveDatastore extends Datastore {

  private static final Logger logger =
      Logger.getLogger(LiveDatastore.class.getName());
  
  private static final PersistenceManagerFactory PERSISTENCE_FACTORY = JDOHelper
      .getPersistenceManagerFactory("transactions-optional");

  private static final String STATS_CACHE_KEY = "stats";
  
  private static Cache STATS_CACHE = null;
  
  static {
    try {
      STATS_CACHE = CacheManager.getInstance().getCacheFactory().createCache(
              ImmutableMap.of(GCacheFactory.EXPIRATION_DELTA, 24 * 60 * 60L));
    } catch (CacheException err) {
      logger.log(Level.SEVERE, "Could not initialize STATS_CACHE", err);
    }
  }

  // Created transiently for each request.
  private PersistenceManager manager;
  
  @Override
  public Channel getChannelByName(String name) {
    try {
      return manager.getObjectById(Channel.class, name);
    } catch (JDOObjectNotFoundException notFound) {
      return null;
    }
  }
  
  @Override
  public PersistentConfiguration getPersistentConfig() {
    try {
      // We often get the PersistentConfiguration before starting the request,
      // manager is not populated yet. Create a one-off manager just for getting
      // the configuration. Since the result is cached by {@link Configuration},
      // it doesn't matter, efficiency-wise.
      PersistenceManager tempManager =
          PERSISTENCE_FACTORY.getPersistenceManager();
      PersistentConfiguration persistentConfig = 
          tempManager.getObjectById(PersistentConfiguration.class, "config");
      tempManager.close();
      return persistentConfig;
    } catch (JDOObjectNotFoundException e) {
      return null;
    }
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
    query.declareParameters("String channelNameParam");

    try {
      return (List<Target>) query.execute(channelName);
    } finally {
      query.closeAll();
    }
  }

  @Override
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
  public void put(Object o) {
    manager.makePersistent(o);
  }

  @Override
  public void putAll(Collection<Object> objects) {
    manager.makePersistentAll(objects);
  }

  @Override
  public void delete(Object o) {
    manager.deletePersistent(o);
  }

  @Override
  public void endRequest() {
    manager.close();
  }

  @Override
  public void startRequest() {
    manager = PERSISTENCE_FACTORY.getPersistenceManager();
  }

  int countUsersActiveInLastNDays(DatastoreService ds, int numDays) {
    com.google.appengine.api.datastore.Query q = 
      new com.google.appengine.api.datastore.Query("User");
    q.setKeysOnly();
    if (numDays > 0) {
      // This is ridiculous, but 30 days in milliseconds is 2.5B, and if numDays is
      // in int, the expression below overflows and we look for
      // lastSeen > some-future-date. To fix, just cast it to a long. 
      long numDays64Bit = numDays;
      q.addFilter("lastSeen", FilterOperator.GREATER_THAN,
                  new Date(System.currentTimeMillis() - numDays64Bit*24*60*60*1000));
    }

    // this is sad, I'm fetching everything and I just want a count. But this query should work according to 
    // http://groups.google.com/group/google-appengine-java/browse_thread/thread/f97bdd5bdf91c114/43035e4eea644c6b?#43035e4eea644c6b
    PreparedQuery pq = ds.prepare(q);
    FetchOptions fetchOptions = FetchOptions.Builder.withOffset(0); 
    return pq.asList(fetchOptions).size(); 
  }


  @Override
  public Datastore.Stats getStats(boolean useCache) {
    if (useCache) {
      try {
        Stats cachedStats = (Stats) STATS_CACHE.get(STATS_CACHE_KEY);
        if (cachedStats != null) {
          return cachedStats;
        }
        logger.info("Stats not in cache, re-computing");
      } catch (InvalidValueException err) {
        logger.log(Level.WARNING, "Could not load data from memcache", err);
      }
    }
    
    Stats ret = new Stats();
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery pq = datastore.prepare(new com.google.appengine.api.datastore.Query("__Stat_Kind__"));
    for (Entity kindStat : pq.asIterable()) {
      String kind = (String)kindStat.getProperty("kind_name");
      if ("Channel".equals(kind)) {
        ret.numChannels = ((Long)kindStat.getProperty("count")).intValue();
        ret.timestamp = (Date)kindStat.getProperty("timestamp");
      }
    }

    ret.numUsers = countUsersActiveInLastNDays(datastore, -1);
    ret.oneDayActiveUsers = countUsersActiveInLastNDays(datastore, 1);
    ret.sevenDayActiveUsers = countUsersActiveInLastNDays(datastore, 7);
    ret.thirtyDayActiveUsers = countUsersActiveInLastNDays(datastore, 30);
    
    STATS_CACHE.put(STATS_CACHE_KEY, ret);
    
    return ret;
  }
  
  private static class ExtractingKeyIterable implements Iterator<String> {
    private Iterator<Entity> wrapped;
    
    public ExtractingKeyIterable(Iterator<Entity> wrapped) {
      this.wrapped = wrapped;
    }
    
    public boolean hasNext() {
      return wrapped.hasNext();
    }
    
    public String next() {
      Key next = wrapped.next().getKey();
      return next.getName();
    }
    
    public void remove() {
      throw new UnsupportedOperationException("remove isn't supported");
    }
  }
  
  @Override
  public Iterator<String> getAllEntityKeys(
      Class<?> entityClass, String lastKey) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    com.google.appengine.api.datastore.Query q = 
        new com.google.appengine.api.datastore.Query(entityClass.getName());
    q.setKeysOnly();
    if (lastKey != null) {
      q.addFilter("name", FilterOperator.GREATER_THAN, lastKey);
    }
    PreparedQuery pq = datastore.prepare(q);
    FetchOptions fetchOptions = FetchOptions.Builder.withOffset(0);
    return new ExtractingKeyIterable(pq.asIterator(fetchOptions));
  }
}
