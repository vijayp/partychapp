package com.imjasonh.partychapp.datastoretask;


import java.util.List;

import com.google.appengine.api.labs.taskqueue.Queue;
import com.imjasonh.partychapp.WebRequest;

public abstract class DatastoreTask {
  public abstract void handle(WebRequest req, Queue q);
  
  public static List<String>keys(WebRequest req) {
    return req.getParameterValues("key");
  }
}
