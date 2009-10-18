package com.imjasonh.partychapp.server;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.xmpp.JID;
import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Configuration;
import com.imjasonh.partychapp.Datastore;

@SuppressWarnings("serial")
public class WebServlet extends HttpServlet {

  @SuppressWarnings("unused")
  private static final Logger LOG = Logger.getLogger(WebServlet.class.getName());

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();
  
    resp.getWriter().write("<style>body { font-family: Helvetica, sans-serif }</style>");
    String name = req.getParameter("name");
    Datastore datastore = Datastore.instance();
    datastore.startRequest();
    Channel channel = datastore.getChannelByName(name);
    if (channel != null) {
      resp.getWriter().write("Sorry, room name is taken");
      return;
    }
    // TODO: Get this programatically
    JID serverJID = new JID(name + "@" + Configuration.chatDomain);
    channel = new Channel(serverJID);
    channel.addMember(new JID(user.getEmail())); // need / ?
    SendUtil.invite(user.getEmail(), serverJID);
    
    // works for "true" ignoring case
    if (Boolean.parseBoolean(req.getParameter("inviteOnly"))) {
      channel.setInviteOnly(true);
    }
    
    for (String invitee : req.getParameter("invitees").split(",")) {
      invitee = invitee.trim();
      if (!invitee.isEmpty()) {        
        resp.getWriter().write("Could not add " + invitee + ". It is not a valid email address.");

        channel.invite(invitee);
        SendUtil.invite(invitee, serverJID);
      }
    }

    channel.put();
    datastore.endRequest();
    resp.getWriter().write(
        "Created! Just accept the chat request and start talking. " +
    		"To add users later, type '/invite user@whatever.com'.");
  }
}
