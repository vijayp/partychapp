package com.imjasonh.partychapp.datastoretask;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.TaskOptions;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.WebRequest;

public class DatastoreTaskMaster extends DatastoreTask {
  private static final Logger LOG = Logger.getLogger(DatastoreTaskMaster.class.getName());
  
  public enum Action {
    MASTER_TASK(new DatastoreTaskMaster()),
    FIX_CHANNELS(new FixChannelsTask());
    
    public DatastoreTask datastoreTask;
    
    private Action(DatastoreTask t) {
      this.datastoreTask = t;
    }
  }
  
  public static final int kNumPerBatch = 10;
  
  @Override
  public void handle(WebRequest req, Queue q) {
    long startTime = System.currentTimeMillis();
    
    // get a JDO extent. this only works with LiveDatastore, so cast away.
    // grab N items, get their keys and make a JSON array of their names
    // add a task with that array as an arg
    try {
      Action act = Action.valueOf(req.getParameter("act"));

      // This is only passed if we break and resume.
      String lastKeyHandled = req.getParameter("lastKeyHandled");

      String maxParam = req.getParameter("max");
      int max = maxParam != null ? Integer.parseInt(maxParam) : -1;
      
      Datastore.instance().startRequest();

      Iterator<String> keys = Datastore.instance().getAllChannelKeys(lastKeyHandled);
            
      int count = 0;
      boolean suppressContinuation = false;
      TaskOptions opts = TaskOptions.Builder.url("/tasks/" + act.name())
                            .method(Method.GET);
      while (keys.hasNext()) {
        lastKeyHandled = keys.next();
        System.err.println(lastKeyHandled);
        ++count;
        if ((max > 0) && (count > max)) {
          suppressContinuation = true;
          break;
        }

        opts.param("key", lastKeyHandled);
        if ((count % kNumPerBatch) == 0) {
          q.add(opts);

          opts = TaskOptions.Builder.url("/tasks/" + act.name()).method(Method.GET);
        }
        
        // cut ourselves off after 20 seconds. we don't want app engine to kill us.
        if ((System.currentTimeMillis() - startTime) > 20*1000) {
          break;
        }
      }
      if ((count % kNumPerBatch) != 0) {
        q.add(opts);
        opts = TaskOptions.Builder.url("/tasks/" + act.name()).method(Method.GET);
      }
      if (count < max) {
        suppressContinuation = true;
      }
      if (count != 0) {
        if (!suppressContinuation) {
          LOG.log(Level.WARNING,
                  "created sub-tasks for " + count + " objects. creating " +
                  "replacement task for remaining objects. lastKeyHandled = " +
                  lastKeyHandled);
          // just add a replacement task, and only end when we try and there's nothing else remaining.
          q.add(TaskOptions.Builder.url("/tasks/" + Action.MASTER_TASK.name())
                .param("act", act.name())
                .param("lastKeyHandled", lastKeyHandled));          
        } else {
          LOG.log(Level.WARNING,
                  "created sub-tasks for " + count + " objects. suppressing replacement. " +
                  "lastKeyHandled = " + lastKeyHandled);
        }
      } else {
        LOG.log(Level.WARNING,
                "all done! lastKeyHandled = " + lastKeyHandled);
      }
    } finally {
      Datastore.instance().endRequest();
    }
  }
}
