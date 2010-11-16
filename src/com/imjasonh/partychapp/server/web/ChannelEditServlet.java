package com.imjasonh.partychapp.server.web;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Allows invite-only and logging to be turned on/off for a channel (by a member
 * of that channel). 
 *
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class ChannelEditServlet extends HttpServlet {
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();

    String channelName = req.getParameter("name");
    Datastore datastore = Datastore.instance();
    try {
      datastore.startRequest();
      Channel channel =
          datastore.getChannelIfUserPresent(channelName, user.getEmail());
      if (channel == null) {
        resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        return;
      }
      
      channel.setInviteOnly(
          Boolean.parseBoolean(req.getParameter("inviteonly")));
      channel.setLoggingDisabled(
          !Boolean.parseBoolean(req.getParameter("logging")));
      
      channel.put();
      
      resp.sendRedirect("/channel/" + channelName);
    } finally {
      datastore.endRequest();
    }    
  }
}
