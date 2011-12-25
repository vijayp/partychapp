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
 * Broadcast a message to a channel, by name.
 *
 * @author vijayp@vijayp.ca (Vijay Pandurangan)
 */
public class BroadcastServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    String channelName = req.getParameter("channel");
    String message = req.getParameter("message");
    CachingDatastore cachingDatastore = WrappingDatastore.findWrappedInstance(
        Datastore.instance(), CachingDatastore.class);
    message = "[ADMIN MESSAGE] " + message;
    if (null != channelName && null != message) {
      resp.setContentType("text/plain");
      cachingDatastore.startRequest();
      try {
        Channel channel = cachingDatastore.getChannelByName(channelName);
        if (null != channel) {
          channel.broadcastIncludingSender(message, null);
          resp.getWriter().write("Successfully wrote message " + message + " to channel " + channelName);
        } else {
          resp.getWriter().write("Could not find channel " + channelName);
        }
      } finally {
        cachingDatastore.endRequest();
      }
    } else {
      resp.setContentType("text/html");
      resp.getWriter().write("<form action='/admin/broadcast'> "
          +"<br/> Channel <input type='text' name='channel' id='channel' />"
          +"<br/> Message <input type='text' name='message' id='message' />"
          +"<br/> <input type='submit' /></form>"
          );
    }
  }
}
