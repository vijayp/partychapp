package com.imjasonh.partychapp.datastoretask;

import java.util.List;

import com.google.appengine.api.labs.taskqueue.TaskHandle;
import com.google.common.collect.Lists;

public class FakeQueue extends TestableQueue {
  List<TestableQueue.Options> addedTasks = Lists.newArrayList();
  
  @Override
  public TaskHandle add(Options opts) {
    addedTasks.add(opts);
    return null;
  }
  
  public List<TestableQueue.Options> getTasks() {
    return addedTasks;
  }
}
