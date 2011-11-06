package com.imjasonh.partychapp.server.admin;

import com.google.common.base.Strings;

import com.imjasonh.partychapp.CachingDatastore;
import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.WrappingDatastore;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Migrate a channel, by name.
 *
 *
 * @author vijayp@vijayp.ca (Vijay Pandurangan)
 */
public class MigrateServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    String channelName = req.getParameter("channel");
    String message = req.getParameter("message");
    if (message == null || message.isEmpty()) {
      message = new String("Your channel has been migrated to this "
          + " new server.");
    }
    if (channelName != null) {
      Channel c = null;
      Datastore ds = Datastore.instance();
      ds.startRequest();
      c = ds.getChannelByName(channelName);
      message = "[ADMIN MESSAGE] " + message;
      if (null != c) {
        c.setMigrated(true);
        ds.put(c);
        CachingDatastore cachingDatastore = WrappingDatastore.findWrappedInstance(
            Datastore.instance(), CachingDatastore.class);

        if (cachingDatastore != null) {      
          cachingDatastore.startRequest();
          cachingDatastore.invalidateCacheIfNecessary(c);
          cachingDatastore.endRequest();
        }
        c.broadcastIncludingSender(message);
        resp.setContentType("text/plain");
        resp.getWriter().write("migrated channel " + c.getName());
      } else {
        resp.setContentType("text/plain");
        resp.getWriter().write("couldn't find channel " + channelName);
      }
    } else {
      resp.setContentType("text/html");
      resp.getWriter().write("<h1> migrate channel </h1>" +
          "<form action='/admin/migrate'> "
          +"<br/> Channel <input type='text' name='channel' id='channel' />"
          +"<br/> Message <input type='text' name='message' id='message' />"
          +"<br/> <input type='submit' /></form>"
          );
    }

  }



}
