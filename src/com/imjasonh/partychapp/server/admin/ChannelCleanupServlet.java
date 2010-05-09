package com.imjasonh.partychapp.server.admin;

import com.google.common.base.Strings;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Member;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Removes transient/non-essential information stored in a {@link Channel}.
 * 
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class ChannelCleanupServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    if (Strings.isNullOrEmpty(req.getPathInfo())) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    // Strip trailing slash to get channel name
    String channelName = req.getPathInfo().substring(1);

    Datastore datastore = Datastore.instance();
    datastore.startRequest();
    try {
      Channel channel = datastore.getChannelByName(channelName);

      if (channel == null) {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
      }

      resp.setContentType("text/plain; charset=utf-8");
      int lastMessageCount = 0;
      long lastMessageSize = 0;
      
      for (Member member : channel.getMembers()) {
        for (String message : member.getLastMessages()) {
          lastMessageCount++;
          lastMessageSize += message.length();
        }
        member.clearLastMessages();
      }
      
      channel.put();
      
      resp.getWriter().write(
          "Cleared away " + lastMessageCount + " recent messages " +
          "from " + channel.getMembers().size() + " members " +
          "totalling " + lastMessageSize + " chars");
    } finally {
      datastore.endRequest();
    }
  }
}
