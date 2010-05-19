package com.imjasonh.partychapp.server.web;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Member;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class LeaveChannelServlet extends HttpServlet {

  @SuppressWarnings("unused")
  private static final Logger logger =
      Logger.getLogger(LeaveChannelServlet.class.getName());

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
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

      Member member = channel.getMemberByJID(user.getEmail());
      String broadcast =
          member.getAlias() + " has left the room (" + member.getJID() + ")";
      channel.broadcast(broadcast, member);

      channel.removeMember(datastore.getUserByJID(user.getEmail()));
      channel.put();
      
      resp.sendRedirect("/");
    } finally {
      datastore.endRequest();
    }
  }
}
