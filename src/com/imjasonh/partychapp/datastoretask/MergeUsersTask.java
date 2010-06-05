package com.imjasonh.partychapp.datastoretask;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.User;
import com.imjasonh.partychapp.WebRequest;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Placeholder for a task that merges users that differ only in capitalization.
 *
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class MergeUsersTask extends DatastoreTask {
  
  private static final Logger logger =
      Logger.getLogger(MergeUsersTask.class.getName());

  @Override
  public void handle(WebRequest req, TestableQueue q) {
    List<String> keys = keys(req);
    int dirtyCount = 0;
    for (String key : keys) {
      User user = Datastore.instance().getUserByJID(key);
      if (user == null) {
        logger.warning("Was not able to find user " + key +
            ", may have been deleted by another task.");
        continue;
      }
      
      logger.warning("Would be merging " + key + " and " + key.toLowerCase());
    }
  }

  /**
   * We are only interested in JIDs that are not all lower-case, so that we can
   * merge them with the all lower-case equivalent.
   */
  @Override
  public Iterator<String> getKeyIterator(String lastKeyHandled) {
    return Iterators.filter(
        Datastore.instance().getAllEntityKeys(User.class, lastKeyHandled),
        new Predicate<String>() {
          @Override
          public boolean apply(String key) {
            return !key.toLowerCase().equals(key);
          }
        });
  }  
  
}
