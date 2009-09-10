package com.imjasonh.partychapp.server;

import java.io.IOException;

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
import com.imjasonh.partychapp.server.command.Command;

@SuppressWarnings("serial")
public class PartychappServlet extends HttpServlet {

  // private static final Logger LOG = Logger.getLogger(PartychappServlet.class.getName());

  private XMPPService XMPP;

  private JID userJID;

  private JID serverJID;

  private Channel channel;

  private Member member;

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    super.doPost(req, resp);

    XMPP = XMPPServiceFactory.getXMPPService();

    Message message = XMPP.parseMessage(req);

    userJID = message.getFromJid();

    serverJID = message.getRecipientJids()[0]; // should only be "to" one jid, right?
    String channelName = serverJID.getId().split("@")[0];

    String body = message.getBody().trim();

    if (channelName.equalsIgnoreCase("echo")) {
      handleEcho(body);
      return;
    }

    channel = Channel.getByName(channelName);
    if (channel == null) {
      // channel doesn't exist yet
      handleCreateChannel(channelName);
    }

    member = channel.getMemberByJID(userJID);
    if (member == null) {
      // room exists, user isn't in room yet
      handleJoinChannel();
    }

    for (Command command : Command.values()) {
      if (command.matches(body)) {
        command.run(body, userJID, serverJID, member, channel);
        return;
      }
    }

    handleMessage(body);
  }

  private void handleMessage(String body) {
    String msg = "['" + member.getAlias() + "'] " + body;
    SendUtil.broadcast(msg, channel, userJID, serverJID);
  }

  private void handleJoinChannel() {
    member = new Member(userJID);
    channel.addMember(member);
    channel.put();

    String youMsg = "You have joined '" + channel.getName() + "' with the alias '"
        + member.getAlias() + "'";
    SendUtil.sendDirect(youMsg, userJID, serverJID);

    String msg = member.getJID() + "has joined the channel with the alias '"
        + member.getAlias() + "'";
    SendUtil.broadcast(msg, channel, userJID, serverJID);
  }

  private void handleCreateChannel(String channelName) {
    channel = new Channel(channelName);
    member = new Member(userJID);
    channel.addMember(member);
    channel.put();
    String msg = "The channel '" + channel.getName() + "' has been created, " +
        "and you have joined with the alias '" + member.getAlias() + "'";
    SendUtil.sendDirect(msg, userJID, serverJID);
  }

  private void handleEcho(String body) {
    // if the user is talking to echo@, just echo back as simply as possible.
    XMPP.sendMessage(new MessageBuilder()
        .withRecipientJids(userJID)
        .withFromJid(serverJID)
        .withBody("echo: " + body)
        .build());
  }
}
