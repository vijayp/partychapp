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
import com.google.appengine.repackaged.com.google.common.base.Preconditions;
import com.imjasonh.partychapp.Channel;

@SuppressWarnings("serial")
public class PartychappServlet extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(PartychappServlet.class.getName());

  private static final XMPPService XMPP = XMPPServiceFactory.getXMPPService();

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    super.doPost(req, resp);

    Message message = XMPP.parseMessage(req);

    JID userJID = message.getFromJid();
    String alias = userJID.getId().split("@")[0]; // TODO don't automatically assume alias from JID

    JID serverJID = message.getRecipientJids()[0]; // should only be "to" one jid, right?
    String channelName = serverJID.getId().split("@")[0];

    String body = message.getBody().trim();

    if (channelName.equals("echo")) {
      // if the user is talking to echo@, just echo back
      sendMessage("ECHO: " + body, serverJID, userJID);
      return;
    }

    Channel channel = Channel.getByName(channelName);
    if (channel == null) {
      // channel doesn't exist yet
      channel = new Channel(channelName);
      channel.addMember(userJID);
      channel.put();
      String msg = "The channel '" + channelName + "' has been created, " +
          "and you have joined with the alias '" + alias + "'";
      sendMessage(msg, serverJID, userJID);
      return;
    }

    Preconditions.checkNotNull(channel);
    if (!channel.isMember(userJID)) {
      // user isn't in room yet
      channel.addMember(userJID);
      channel.put();
      String msg = "You have joined '" + channelName + "' with the alias '" + alias + "'";
      sendMessage(msg, serverJID, userJID);
    }

    if (body.equals("/leave")) {
      channel.removeMember(userJID);
      channel.put();
      sendMessage("You have left '" + channelName + "'", serverJID, userJID);

      if (!channel.isEmpty()) {
        // tell everyone else (if they exist)
        String msg = alias + " has left the room (" + userJID.getId() + ")";
        sendMessage(msg, serverJID, channel.getAllMemberJIDsArray());
      }
    } else if (body.equals("/list")) {
      StringBuilder sb = new StringBuilder()
          .append("List of members of '" + channelName + "':").append('\n');
      for (JID member : channel.getAllMemberJIDsArray()) {
        sb.append("* ").append(member.getId()).append('\n');
      }
      sendMessage(sb.toString(), serverJID, userJID);
    } else if (body.equals("/commands") || body.equals("/help")) {
      String msg = new StringBuilder()
          .append("List of available commands:\n")
          .append("* /list prints a list of all members subscribed to this channel\n")
          .append("* /leave unsubscribes you from this channel\n")
          .append("* Send a message to echo@... to hear yourself talk")
          .toString();
      sendMessage(msg, serverJID, userJID);
    } else {
      // user is in room now, send message to everyone else (if they exist)
      JID[] recipients = channel.getAllMemberJIDsArrayExcept(userJID);
      if (recipients.length > 0) {
        sendMessage("['" + alias + "'] " + body, serverJID, recipients);
      }
    }
  }

  private static void sendMessage(String body, JID fromJID, JID... toJID) {
    XMPP.sendMessage(new MessageBuilder()
        .withRecipientJids(toJID)
        .withFromJid(fromJID)
        .withBody(body)
        .build());
  }
}
