package com.imjasonh.partychapp.datastoretask;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.JDOHelper;

import com.google.appengine.api.labs.taskqueue.Queue;
import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.WebRequest;

public class FixChannelsTask extends DatastoreTask {
  private static final Logger LOG = Logger.getLogger(FixChannelsTask.class.getName());
 
  
  public void handle(WebRequest url, Queue q) {
    List<String> keys = keys(url);
    int count = 0;
    for (String key : keys) {
      Channel c = Datastore.instance().getChannelByName(key);
      if (JDOHelper.isDirty(c)) {
        ++count;
      }
    }
    LOG.log(Level.WARNING, "Handled " + keys.size() + " keys. Put " + count + " objects to datastore");
  }
}
