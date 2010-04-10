package com.imjasonh.partychapp.server.web;

import com.google.appengine.api.labs.taskqueue.QueueFactory;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.WebRequest;
import com.imjasonh.partychapp.datastoretask.DatastoreTaskMaster;
import com.imjasonh.partychapp.datastoretask.TestableQueue;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CronAndTasksServlet  extends HttpServlet {
  public static final long serialVersionUID = 985749740983755L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
    try {
      Datastore.instance().startRequest();
      
      String[] paths = req.getRequestURI().split("/");
      String taskName = paths[paths.length - 1];
      
      DatastoreTaskMaster.Action act = DatastoreTaskMaster.Action.valueOf(taskName);
      if (act != null) {
        act.datastoreTask.handle(new WebRequest(req),
                                 new TestableQueue(QueueFactory.getDefaultQueue()));
      } else {
        throw new RuntimeException("unknown task type " + taskName);
      }
    } finally {
      Datastore.instance().endRequest();
    }
  }
}
