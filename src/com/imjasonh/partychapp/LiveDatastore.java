package com.imjasonh.partychapp;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.imjasonh.partychapp.ppb.Reason;
import com.imjasonh.partychapp.ppb.Target;

public class LiveDatastore extends Datastore {

  private static final PersistenceManager PERSISTENCE_MANAGER = JDOHelper
      .getPersistenceManagerFactory("transactions-optional")
      .getPersistenceManager();

  @Override
  public Channel getChannelByName(String name) {
    try {
      Channel channel = PERSISTENCE_MANAGER.getObjectById(Channel.class, name);
      return channel;
    } catch (JDOObjectNotFoundException notFound) {
      return null;
    }
  }

  @Override
  public Target getTargetByID(String key) {
    try {
      return PERSISTENCE_MANAGER.getObjectById(Target.class, key);
    } catch (JDOObjectNotFoundException e) {
      // TODO(nsanch): there has to be a better way
      return null;
    }
  }

  @Override
  public Target getTarget(Channel channel, String name) {
    return getTargetByID(Target.createTargetKey(name, channel));
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
    Query query = PERSISTENCE_MANAGER.newQuery(Reason.class);
    query.setFilter("targetId == targetIdParam");
    query.setOrdering("timestamp desc");
    query.declareParameters("String targetIdParam");
    query.setRange(0, limit);

    List<Reason> reasons = (List<Reason>) query.execute(target.key());
    query.closeAll();
    return reasons;
  }

  @Override
  public void put(Serializable s) {
    PERSISTENCE_MANAGER.makePersistent(s);
  }

  @Override
  public void putAll(Collection<? extends Serializable> objects) {
    PERSISTENCE_MANAGER.makePersistentAll(objects);
  }

  @Override
  public void delete(Serializable s) {
    PERSISTENCE_MANAGER.deletePersistent(s);
  }
}
