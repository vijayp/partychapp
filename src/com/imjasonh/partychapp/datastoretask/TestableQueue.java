package com.imjasonh.partychapp.datastoretask;

import java.util.Collections;
import java.util.List;

import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.TaskHandle;
import com.google.appengine.api.labs.taskqueue.TaskOptions;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;
import com.google.common.collect.Lists;

public class TestableQueue {
  private Queue wrapped = null;

  public static class Options {
    private String url;
    private List<String> params = Lists.newArrayList();
    private TaskOptions opts = TaskOptions.Builder.method(Method.GET);
    
    public Options(String url) {
      this.url(url);
    }
    
    public Options url(String url) {
      this.url = url;
      opts.url(url);
      return this;
    }
    
    public Options param(String name, String value) {
      opts.param(name, value);
      params.add(name + "=" + value);
      return this;
    }
    
    public TaskOptions asTaskOptions() {
      return opts;
    }
    
    public List<String> params() {
      return Collections.unmodifiableList(params);
    }
    
    public String url() {
      return url;
    }
    
    @Override
    public String toString() {
      return "[TestableQueue.Options: url = " + url + ", params = " + params + "]";
    }
  }
  
  // For test subclass
  TestableQueue() {}
  
  public TestableQueue(Queue q) {
    wrapped = q;
  }
  
  public TaskHandle add(Options opts) {
    return wrapped.add(opts.asTaskOptions());
  }
}
