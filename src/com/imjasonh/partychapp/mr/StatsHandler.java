package com.imjasonh.partychapp.mr;

import java.io.IOException;
import com.google.appengine.api.datastore.Text;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;

import com.imjasonh.partychapp.Datastore;

public class StatsHandler extends HttpServlet {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Query qu = new Query("stats_table");
    
    if (null == req.getParameter("table")) {
      resp.setContentType("text/html");
      PreparedQuery pq = ds.prepare(qu);
      for (Entity result: pq.asIterable()) {
        if (result.hasProperty("title") && result.hasProperty("csv")) {
          resp.getWriter().print(
              "<a href='/admin/stats?table=" 
          // TODO ESCAPE THIS! THIS IS DANGEROUS
          + StringEscapeUtils.escapeHtml(result.getProperty("title").toString()) 
          + "'>" 
          + result.getProperty("title").toString()
          + "<br/>");
        }
      }
      return;
    }
    qu.addFilter("title", FilterOperator.EQUAL, req.getParameter("table"));
    resp.setContentType("text/html");
    PreparedQuery pq = ds.prepare(qu);
    for (Entity result: pq.asIterable()) {
      if (result.hasProperty("title") && result.hasProperty("csv")) {
        resp.getWriter().write("<b>" + (String)result.getProperty("title") + "</b><br />");
        Object img1 = result.getProperty("image_url1");
        Object img2 = result.getProperty("image_url2");
        if (null != img1) {
          resp.getWriter().write("<img src='"+StringEscapeUtils.escapeHtml((String)img1) + "' /> <br/>");
        }
        if (null != img2) {
          resp.getWriter().write("<img src='"+StringEscapeUtils.escapeHtml((String)img2) + "' /> <br/>");
        }
        
        resp.getWriter().write("<br/>");
        resp.getWriter().write(StringEscapeUtils.escapeHtml(((Text)result.getProperty("csv")).getValue()));
        resp.getWriter().write("<br/>");
        resp.getWriter().write("<br/>");
      }
    }
  }
}
