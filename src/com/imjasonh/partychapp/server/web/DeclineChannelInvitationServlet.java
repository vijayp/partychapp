package com.imjasonh.partychapp.server.web;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class DeclineChannelInvitationServlet extends HttpServlet {

  @SuppressWarnings("unused")
  private static final Logger logger =
      Logger.getLogger(DeclineChannelInvitationServlet.class.getName());

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
      
      if (!channel.getInvitees().contains(user.getEmail())) {
        resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        return;        
      }
      
      channel.removeInvitee(user.getEmail());
      channel.put();
      
      resp.sendRedirect("/");
    } finally {
      datastore.endRequest();
    }
  }
}
