package com.google.appengine.api.labs.taskqueue;

import java.io.UnsupportedEncodingException;
import java.util.List;

import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Param;
import com.google.appengine.repackaged.com.google.common.collect.Lists;

public class TestQueue implements Queue {
  List<String> addedTasks = Lists.newArrayList();
  
  public TaskHandle add(TaskOptions taskOptions) {
    try {
      String url = taskOptions.getUrl();
      boolean first = true;
      for (Param p : taskOptions.getParams()) {
        if (first) {
          first = false;
          url += "?";
        } else {
          url += "&";
        }
        url += p.getURLEncodedName() + "=" + p.getURLEncodedValue();
      }
      addedTasks.add(url);
    } catch (UnsupportedEncodingException e) {}
    return null;
  }
  
  public List<String> getTasks() {
    return addedTasks;
  }

  public TaskHandle add() {
    throw new UnsupportedOperationException();
  }
  
  public TaskHandle add(Transaction txn, TaskOptions taskOptions) {
    throw new UnsupportedOperationException();
  }

  public String getQueueName() {
    throw new UnsupportedOperationException();
  }

}
