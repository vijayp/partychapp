package com.imjasonh.partychapp.datastoretask;


import java.util.List;

import com.imjasonh.partychapp.WebRequest;

public abstract class DatastoreTask {
  public enum Action {
    MASTER_TASK(new DatastoreTaskMaster()),
    FIX_CHANNELS(new FixChannelsTask()),
    STATS_CRON_JOB(new StatsCronJob());
    
    public DatastoreTask datastoreTask;
    
    private Action(DatastoreTask t) {
      this.datastoreTask = t;
    }
  }

  public abstract void handle(WebRequest req, TestableQueue q);
  
  public static List<String>keys(WebRequest req) {
    return req.getParameterValues("key");
  }
}
