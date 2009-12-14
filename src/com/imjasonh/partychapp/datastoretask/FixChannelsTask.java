package com.imjasonh.partychapp.datastoretask;

import java.util.List;

import javax.jdo.JDOHelper;

import org.mortbay.log.Log;

import com.google.appengine.api.labs.taskqueue.Queue;
import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.WebRequest;

public class FixChannelsTask extends DatastoreTask {
  public void handle(WebRequest url, Queue q) {
    List<String> keys = keys(url);
    int count = 0;
    for (String key : keys) {
      Channel c = Datastore.instance().getChannelByName(key);
      if (JDOHelper.isDirty(c)) {
        ++count;
      }
    }
    Log.warn("Handled " + keys.size() + " keys. Put " + count + " objects to datastore");
  }
}
