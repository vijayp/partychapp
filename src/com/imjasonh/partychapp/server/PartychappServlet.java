package com.imjasonh.partychapp.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.Message;
import com.google.appengine.api.xmpp.XMPPService;
import com.google.appengine.api.xmpp.XMPPServiceFactory;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.server.command.Command;
import com.imjasonh.partychapp.server.command.CommandHandler;
import com.imjasonh.partychapp.server.command.CreateAndJoinCommand;
import com.imjasonh.partychapp.server.command.JoinCommand;

@SuppressWarnings("serial")
public class PartychappServlet extends HttpServlet {

  // private static final Logger LOG = Logger.getLogger(PartychappServlet.class.getName());

  private XMPPService XMPP;

  com.imjasonh.partychapp.Message message;

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    super.doPost(req, resp);

    XMPP = XMPPServiceFactory.getXMPPService();

    Message xmppMessage = XMPP.parseMessage(req);
    doXmpp(xmppMessage);
  }

  public void doXmpp(Message xmppMessage) {
    JID userJID = xmppMessage.getFromJid();

    JID serverJID = xmppMessage.getRecipientJids()[0]; // should only be "to" one jid, right?
    String channelName = serverJID.getId().split("@")[0];

    String body = xmppMessage.getBody().trim();

    message = new com.imjasonh.partychapp.Message(body, userJID, serverJID, null, null);

    if (channelName.equalsIgnoreCase("echo")) {
      handleEcho(body);
      return;
    }

    message.channel = Datastore.instance().getChannelByName(channelName);
    if (message.channel == null) {
      // channel doesn't exist yet
      new CreateAndJoinCommand().doCommand(message);
    }

    message.member = message.channel.getMemberByJID(userJID);
    if (message.member == null) {
      // room exists, user isn't in room yet
      new JoinCommand().doCommand(message);
    }

    CommandHandler handler = Command.getCommandHandler(message);

    if (handler != null) {
      handler.doCommand(message);
    } else {
      handleMessage(body);
    }
  }

  private void handleMessage(String body) {
    message.member.addToLastMessages(body);
    String msg = message.member.getAliasPrefix() + body;
    SendUtil.broadcast(msg, message.channel, message.userJID, message.serverJID);
  }

  private void handleEcho(String body) {
    SendUtil.sendDirect("echo: " + body, message.userJID, message.serverJID);
  }
}
