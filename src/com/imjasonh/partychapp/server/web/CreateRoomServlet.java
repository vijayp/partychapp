package com.imjasonh.partychapp.server.web;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.xmpp.JID;
import com.google.common.collect.Lists;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Configuration;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.server.SendUtil;
import com.imjasonh.partychapp.server.command.InviteHandler;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class CreateRoomServlet extends HttpServlet {

  @SuppressWarnings("unused")
  private static final Logger logger =
      Logger.getLogger(CreateRoomServlet.class.getName());

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();
  
    resp.getWriter().write("<style>body { font-family: Helvetica, sans-serif }</style>");
    String name = req.getParameter("name");
    Datastore datastore = Datastore.instance();
    datastore.startRequest();
    try {
      Channel channel = datastore.getChannelByName(name);
      if (channel != null) {
        resp.getWriter().write("Sorry, room name is taken");
        return;
      }
      
      com.imjasonh.partychapp.User pchapUser =
          datastore.getOrCreateUser(user.getEmail());
      
      // TODO: Get this programatically
      JID serverJID = new JID(name + "@" + Configuration.chatDomain);
      channel = new Channel(serverJID);
      channel.addMember(pchapUser);
      
      SendUtil.invite(user.getEmail(), serverJID);
      
      // works for "true" ignoring case
      if (Boolean.parseBoolean(req.getParameter("inviteonly"))) {
        channel.setInviteOnly(true);
      }
  
      List<String> invitees = Lists.newArrayList();
      if (!req.getParameter("invitees").isEmpty()) {
      	String error = InviteHandler.parseEmailAddresses(req.getParameter("invitees"), invitees);
      	for (String invitee : invitees) {
      		channel.invite(invitee);
      		SendUtil.invite(invitee, serverJID);
      	}
  	    resp.getWriter().write(error);
      } else {
      	resp.getWriter().write("You're the only person in this room for now.<P>");
      }
  
      channel.put();
      resp.getWriter().write(
          "Created! Just accept the chat request and start talking. " +
      		"To add users later, type '/invite user@whatever.com'.");
      
      resp.getWriter().write(
      		"<P>Try messaging <a href=im:" + serverJID.getId() + ">"
      		+ serverJID.getId() + "</a>");
    } finally {
      datastore.endRequest();      
    }
  }
}
