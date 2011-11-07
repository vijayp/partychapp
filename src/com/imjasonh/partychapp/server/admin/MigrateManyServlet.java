package com.imjasonh.partychapp.server.admin;

import com.google.common.base.Strings;

import com.imjasonh.partychapp.CachingDatastore;
import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Datastore.NotImplementedException;
import com.imjasonh.partychapp.LiveDatastore;
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
public class MigrateManyServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    String numChannels = req.getParameter("num_channels");
    String message = req.getParameter("message");
    if (message == null || message.isEmpty()) {
      message = new String("Your channel has been migrated to this "
          + " new server.");
      message = "[ADMIN MESSAGE] " + message;
    }

    if (null != numChannels && !numChannels.isEmpty()) {
      int num_channels = Integer.parseInt(numChannels);
      Datastore ds = new LiveDatastore();
      ds.startRequest();
      try {
        Iterable<Channel> cnls;
        try {
          cnls = ds.getChannelsByMigrationStatus(false);

          for (Channel c : cnls) {
            if (--num_channels < 0) {
              break;
            }
            c.setMigrated(true);
            ds.put(c);
            resp.setContentType("text/plain");
            resp.getWriter().write("migrated channel " + c.getName());
            c.broadcastIncludingSender(message);
          }
        } catch (NotImplementedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      } finally {
        ds.endRequest();
      }
    } else {
      resp.setContentType("text/html");
      resp.getWriter().write("<h1> migrate channel </h1>" +
          "<form action='/admin/migratemany'> "
          +"<br/> Num channels<input type='text' name='num_channels' id='num_channels' />"
          +"<br/> Message <input type='text' name='message' id='message' />"
          +"<br/> <input type='submit' /></form>"
          );
    }

  }



}
