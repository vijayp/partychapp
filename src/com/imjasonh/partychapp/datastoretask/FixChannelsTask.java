package com.imjasonh.partychapp.datastoretask;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.WebRequest;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.JDOHelper;

public class FixChannelsTask extends DatastoreTask {
  private static final Logger logger =
      Logger.getLogger(FixChannelsTask.class.getName());
  
  @Override
  public void handle(WebRequest url, TestableQueue q) {
    List<String> keys = keys(url);
    int dirtyCount = 0;
    for (String key : keys) {
      Channel c = Datastore.instance().getChannelByName(key);
      if (JDOHelper.isDirty(c)) {
        ++dirtyCount;
      }
      c.put();
    }
    logger.warning(
        "Handled " + keys.size() + " keys. " +
        "Modified " + dirtyCount + " objects");
  }

  @Override
  public Iterator<String> getKeyIterator(String lastKeyHandled) {
    return Datastore.instance().getAllEntityKeys(Channel.class, lastKeyHandled);
  }
}
