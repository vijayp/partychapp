package com.imjasonh.partychapp.server.web;

import com.google.appengine.api.taskqueue.QueueFactory;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.WebRequest;
import com.imjasonh.partychapp.datastoretask.DatastoreTask;
import com.imjasonh.partychapp.datastoretask.TestableQueue;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CronAndTasksServlet  extends HttpServlet {
  public static final long serialVersionUID = 985749740983755L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    try {
      Datastore.instance().startRequest();
      
      String[] paths = req.getRequestURI().split("/");
      String taskName = paths[paths.length - 1];
      
      DatastoreTask.Action act = DatastoreTask.Action.valueOf(taskName);
      if (act != null) {
        act.datastoreTask.handle(
              new WebRequest(req), 
              new TestableQueue(QueueFactory.getDefaultQueue()));
      } else {
        resp.sendError(
            HttpServletResponse.SC_BAD_REQUEST,
            "unknown task type " + taskName);
      }
    } finally {
      Datastore.instance().endRequest();
    }
  }
}
