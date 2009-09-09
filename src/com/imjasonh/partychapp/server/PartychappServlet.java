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
import com.imjasonh.partychapp.Member;

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
    } else if (!channel.isMember(userJID)) {
      // room exists, user isn't in room yet
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
    JID[] recipients = channel.getMembersJIDsToSendTo(userJID);
    if (recipients.length > 0) {
      sendMessage("['" + alias + "'] " + body, recipients);
    }
  }

  private void handleHelp() {
    String msg = new StringBuilder()
        .append("List of available commands:\n")
        .append("* /list prints a list of all members subscribed to this channel\n")
        .append("* /leave unsubscribes you from this channel\n")
        .append("* Send a message to echo@... to hear yourself talk")
        .toString();
    sendMessage(msg, userJID);
  }

  private void handleList() {
    StringBuilder sb = new StringBuilder()
        .append("List of members of '" + channelName + "':").append('\n');
    for (Member member : channel.getMembers()) {
      sb.append("* ").append(member.getJID()).append('\n'); // TODO decorate with alias and snooze
      // status
    }
    sendMessage(sb.toString(), userJID);
  }

  private void handleLeave() {
    Member member = channel.removeMember(userJID);
    channel.put();
    sendMessage("You have left '" + channelName + "'", userJID);

    if (!channel.isEmpty()) {
      // tell everyone else
      String msg = member.getAlias() + " has left the room (" + member.getJID() + ")";
      sendMessage(msg, channel.getMembersJIDsToSendTo(userJID));
    }
  }

  private void handleJoinChannel() {
    Member member = channel.addMember(userJID);
    channel.put();
    String youMsg = "You have joined '" + channelName + "' with the alias '" + alias + "'";
    sendMessage(youMsg, userJID);

    if (channel.getMembers().size() > 1) {
      JID[] otherMembers = channel.getMembersJIDsToSendTo(userJID);
      String broadcastMsg = member.getJID() + "has joined the channel with the alias '"
          + member.getAlias() + "'";
      sendMessage(broadcastMsg, otherMembers);
    }
  }

  private Channel handleCreateChannel() {
    Channel channel;
    channel = new Channel(channelName);
    channel.addMember(userJID);
    channel.put();
    String msg = "The channel '" + channelName + "' has been created, " +
        "and you have joined with the alias '" + alias + "'";
    sendMessage(msg, userJID);
    return channel;
  }

  private void handleEcho(String body) {
    // if the user is talking to echo@, just echo back
    sendMessage("ECHO: " + body, userJID);
  }

  private void sendMessage(String body, JID... toJID) {
    XMPP.sendMessage(new MessageBuilder()
        .withRecipientJids(toJID)
        .withFromJid(serverJID)
        .withBody(body)
        .build());
  }
}
