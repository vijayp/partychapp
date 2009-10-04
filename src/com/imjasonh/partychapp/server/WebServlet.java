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
import com.google.appengine.api.xmpp.Message;
import com.google.appengine.api.xmpp.XMPPService;
import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.server.command.Command;

@SuppressWarnings("serial")
public class WebServlet extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(WebServlet.class.getName());

  private XMPPService XMPP;

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();
  
    resp.getWriter().write("<style>body { font-family: Helvetica, sans-serif }</style>");
    boolean isCreate = req.getParameter("Create a new room") != null;
    String name = req.getParameter("name");
    Datastore datastore = Datastore.instance();
    datastore.startRequest();
    Channel channel = datastore.getChannelByName(name);
    if (channel != null) {
      resp.getWriter().write("Sorry, room name is taken");
      return;
    }
    // TODO: Get this programatically
    channel = new Channel(new JID(name + "@partychapp.appspotchat.com"));
    channel.addMember(new JID(user.getEmail())); // need / ?
    SendUtil.invite(user.getEmail(), channel.serverJID());
    
    if ("true".equals(req.getParameter("inviteOnly"))) {
      channel.setInviteOnly(true);
    }
    
    for (String invitee : req.getParameter("invitees").split(",")) {
      invitee = invitee.trim();
      if (!"".equals(invitee)) {
        channel.invite(invitee);
        SendUtil.invite(invitee, channel.serverJID());
      }
    }

    channel.put();
    datastore.endRequest();
    resp.getWriter().write(
        "Created! Just accept the chat request and start talking. " +
    		"To add users later, type '/invite user@whatever.com'.");
  }

  public void doXmpp(Message xmppMessage) {
    Datastore.instance().startRequest();
    
    JID userJID = xmppMessage.getFromJid();

    JID serverJID = xmppMessage.getRecipientJids()[0]; // should only be "to" one jid, right?
    String channelName = serverJID.getId().split("@")[0];

    String body = xmppMessage.getBody().trim();

    com.imjasonh.partychapp.Message message = new com.imjasonh.partychapp.Message(body, userJID, serverJID, null, null);

    if (channelName.equalsIgnoreCase("echo")) {
      handleEcho(message);
      return;
    }

    message.channel = Datastore.instance().getChannelByName(channelName);
    if (message.channel != null) {
      message.member = message.channel.getMemberByJID(userJID);
    }

    Command.getCommandHandler(message).doCommand(message);
    
    Datastore.instance().endRequest();
  }

  private void handleEcho(com.imjasonh.partychapp.Message message) {
    LOG.severe("Body of message sent to echo@ is: " + message.content);
    SendUtil.sendDirect("echo: " + message.content, message.userJID, message.serverJID);
  }
}
