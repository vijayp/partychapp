package com.imjasonh.partychapp.server.web;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.server.InviteUtil;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class GetChannelInvitationServlet extends HttpServlet {

  @SuppressWarnings("unused")
  private static final Logger logger =
      Logger.getLogger(GetChannelInvitationServlet.class.getName());

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();

    String channelName = req.getParameter("name");
    Datastore datastore = Datastore.instance();
    try {
      datastore.startRequest();
      Channel channel = datastore.getChannelByName(channelName);
      if (channel == null) {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
      }

      if (channel.isInviteOnly()) {
        resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        return;        
      }

      channel.invite(user.getEmail());
      channel.put();
      
      channel.broadcastIncludingSender(
          String.format(
              "%s invited themselves to the room.", user.getEmail()));

      InviteUtil.invite(user.getEmail(), channel, "you", "you");
      
      resp.sendRedirect(channel.webUrl());
    } finally {
      datastore.endRequest();
    }
  }
}
