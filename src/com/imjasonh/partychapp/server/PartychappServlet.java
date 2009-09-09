package com.imjasonh.partychapp.server;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.Message;
import com.google.appengine.api.xmpp.MessageBuilder;
import com.google.appengine.api.xmpp.XMPPService;
import com.google.appengine.api.xmpp.XMPPServiceFactory;
import com.imjasonh.partychapp.Channel;

@SuppressWarnings("serial")
public class PartychappServlet extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(PartychappServlet.class.getName());

  private XMPPService XMPP;

  private JID userJID;

  private JID serverJID;

  private String alias;

  private Channel channel;

  private String channelName;

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    super.doPost(req, resp);

    XMPP = XMPPServiceFactory.getXMPPService();

    Message message = XMPP.parseMessage(req);

    userJID = message.getFromJid();
    alias = userJID.getId().split("@")[0]; // TODO don't automatically assume alias from JID

    serverJID = message.getRecipientJids()[0]; // should only be "to" one jid, right?
    channelName = serverJID.getId().split("@")[0];

    String body = message.getBody().trim();

    if (channelName.equals("echo")) {
      handleEcho(body);
      return;
    }

    channel = Channel.getByName(channelName);
    if (channel == null) {
      // channel doesn't exist yet
      channel = handleCreateChannel();
    }
    if (!channel.isMember(userJID)) {
      // user isn't in room yet
      handleJoinChannel();
    }

    // channel exists, user is in it

    if (body.equals("/leave")) {
      handleLeave();
    } else if (body.equals("/list")) {
      handleList();
    } else if (body.equals("/commands") || body.equals("/help")) {
      handleHelp();
    } else {
      // just a normal message to broadcast
      handleMessage(body);
    }
  }

  private void handleMessage(String body) {
    JID[] recipients = channel.getAllMemberJIDsArrayExcept(userJID);
    if (recipients.length > 0) {
      sendMessage("['" + alias + "'] " + body, serverJID, recipients);
    }
  }

  private void handleHelp() {
    String msg = new StringBuilder()
        .append("List of available commands:\n")
        .append("* /list prints a list of all members subscribed to this channel\n")
        .append("* /leave unsubscribes you from this channel\n")
        .append("* Send a message to echo@... to hear yourself talk")
        .toString();
    sendMessage(msg, serverJID, userJID);
  }

  private void handleList() {
    StringBuilder sb = new StringBuilder()
        .append("List of members of '" + channelName + "':").append('\n');
    for (JID member : channel.getAllMemberJIDsArray()) {
      sb.append("* ").append(member.getId()).append('\n');
    }
    sendMessage(sb.toString(), serverJID, userJID);
  }

  private void handleLeave() {
    channel.removeMember(userJID);
    channel.put();
    sendMessage("You have left '" + channelName + "'", serverJID, userJID);

    if (!channel.isEmpty()) {
      // tell everyone else (if they exist)
      String msg = alias + " has left the room (" + userJID.getId() + ")";
      sendMessage(msg, serverJID, channel.getAllMemberJIDsArray());
    }
  }

  private void handleJoinChannel() {
    channel.addMember(userJID);
    channel.put();
    String msg = "You have joined '" + channelName + "' with the alias '" + alias + "'";
    sendMessage(msg, serverJID, userJID);
  }

  private Channel handleCreateChannel() {
    Channel channel;
    channel = new Channel(channelName);
    channel.addMember(userJID);
    channel.put();
    String msg = "The channel '" + channelName + "' has been created, " +
        "and you have joined with the alias '" + alias + "'";
    sendMessage(msg, serverJID, userJID);
    return channel;
  }

  private void handleEcho(String body) {
    // if the user is talking to echo@, just echo back
    sendMessage("ECHO: " + body, serverJID, userJID);
  }

  private void sendMessage(String body, JID fromJID, JID... toJID) {
    XMPP.sendMessage(new MessageBuilder()
        .withRecipientJids(toJID)
        .withFromJid(fromJID)
        .withBody(body)
        .build());
  }
}
