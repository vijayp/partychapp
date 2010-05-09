package com.imjasonh.partychapp.server.admin;

import com.google.common.base.Strings;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Member;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Dumps basic information about a channel.
 * 
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class ChannelServlet extends HttpServlet {
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
      
      Writer writer = resp.getWriter();
      writer.write("Name: " + channel.getName() + "\n");
      writer.write("Invite only: " + channel.isInviteOnly() + "\n");
      writer.write("Invitees:\n");
      for (String invitee : channel.getInvitees()) {
        writer.write("\t" + invitee + "\n");
      }
      writer.write("Members:\n");
      for (Member member : channel.getMembers()) {
        writer.write("\t" + member.getJID() + "\n");
        writer.write("\t\talias:" + member.getAlias() + "\n");
        writer.write("\t\tsnooze:" + member.getSnoozeStatus() + "\n");
        writer.write("\t\tlast messages:\n");
        for (String message : member.getLastMessages()) {
          writer.write("\t\t\t" + message + "\n");
        }
      }
    } finally {
      datastore.endRequest();
    }
  }
}
