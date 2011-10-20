package com.imjasonh.partychapp.mr;

import java.io.IOException;
import com.google.appengine.api.datastore.Text;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

import com.imjasonh.partychapp.Datastore;

public class StatsHandler extends HttpServlet {
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Query qu = new Query("processed_stats");
    resp.setContentType("text/plain");
    PreparedQuery pq = ds.prepare(qu);
    for (Entity result: pq.asIterable()) {
      if (result.hasProperty("title") && result.hasProperty("csv")) {
        resp.getWriter().write((String)result.getProperty("title"));
        resp.getWriter().write("\n");
        resp.getWriter().write(((Text)result.getProperty("csv")).getValue());
        resp.getWriter().write("\n\n\n\n");
      }
    }
  }
}
