package com.imjasonh.partychapp.datastoretask;


import java.util.List;

import com.imjasonh.partychapp.WebRequest;

public abstract class DatastoreTask {
  public abstract void handle(WebRequest req, TestableQueue q);
  
  public static List<String>keys(WebRequest req) {
    return req.getParameterValues("key");
  }
}
