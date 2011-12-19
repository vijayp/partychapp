package com.imjasonh.partychapp.server.web;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.xmpp.JID;
import com.google.common.collect.Lists;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Configuration;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.server.InviteUtil;
import com.imjasonh.partychapp.server.PartychappServlet;
import com.imjasonh.partychapp.server.SendUtil;
import com.imjasonh.partychapp.server.command.InviteHandler;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class CreateChannelServlet extends HttpServlet {

  @SuppressWarnings("unused")
  private static final Logger logger =
      Logger.getLogger(CreateChannelServlet.class.getName());
  
  // See http://tools.ietf.org/html/rfc3920#appendix-A.5 for the list of
  // characters that are not allowed in JIDs
  private static final Pattern ILLEGAL_JID_CHARACTERS =
      Pattern.compile("[ \"&'/:<>@]");

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();
  
    String name = ILLEGAL_JID_CHARACTERS.matcher(
            req.getParameter("name")).replaceAll(".");
    Datastore datastore = Datastore.instance();
    datastore.startRequest();
    try {
      Channel channel = datastore.getChannelByName(name);
      if (channel != null) {
        resp.getWriter().write("Sorry, room name is taken");
        return;
      }

      // Generate server JID and use it immediately (to send the chat invite). 
      // If we somehow end up with an invalid JID (despite sanitizing above), 
      // the request will be aborted now, before we commit anything to the
      // datastore and end up in an inconsistent state.
      JID serverJID = new JID(name + "@" + Configuration.chatDomain);
      // The creator only gets an XMPP invite, not an email one.

      com.imjasonh.partychapp.User pchapUser =
          datastore.getOrCreateUser(user.getEmail());
      
      channel = new Channel(serverJID);
      channel.addMember(pchapUser);
      // auto-migrate new channels
      channel.setMigrated(true);
      
      // works for "true" ignoring case
      if (Boolean.parseBoolean(req.getParameter("inviteonly"))) {
        channel.setInviteOnly(true);
      }
  
      List<String> invitees = Lists.newArrayList();
      if (!req.getParameter("invitees").isEmpty()) {
      	String error = InviteHandler.parseEmailAddresses(
      	    req.getParameter("invitees"), invitees);
      	for (String invitee : invitees) {
      		channel.invite(invitee);
      		InviteUtil.invite(
      		    invitee,
      		    channel,
      		    user.getEmail(),
      		    user.getEmail());
      	}
  	    resp.getWriter().write(error);
      }
  
      channel.put();
      
      // this will not usually get seen
      channel.broadcastIncludingSender("Welcome to your new channel\n"); 
      String cn = channel.getName() + "@" + PartychappServlet.getMigratedDomain(channel.getMigrated());
      resp.getWriter().write(
          "Created! Just accept the chat request and start talking. " +
      		"To add users later, type '/invite user@whatever.com'.");
      
      resp.getWriter().write(
      		"<P>Try messaging <a href=\"im:" + cn + "\">"
      		+ cn + "</a> or visit <a href=\"/channel/" + name
      		+ "\">the room's page</a> for more information.");
    } finally {
      datastore.endRequest();      
    }
  }
}
