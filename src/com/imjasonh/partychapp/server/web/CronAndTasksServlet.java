package com.imjasonh.partychapp.server.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.labs.taskqueue.QueueFactory;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.WebRequest;
import com.imjasonh.partychapp.datastoretask.DatastoreTaskMaster;

public class CronAndTasksServlet  extends HttpServlet {
  public static final long serialVersionUID = 985749740983755L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
          throws ServletException, IOException {
    try {
      Datastore.instance().startRequest();
      
      String[] paths = req.getRequestURI().split("/");
      String taskName = paths[paths.length - 1];
      
      DatastoreTaskMaster.Action act = DatastoreTaskMaster.Action.valueOf(taskName);
      if (act != null) {
        act.datastoreTask.handle(new WebRequest(req), QueueFactory.getDefaultQueue());
      } else {
        throw new RuntimeException("unknown task type " + taskName);
      }
    } finally {
      Datastore.instance().endRequest();
    }
  }
}
