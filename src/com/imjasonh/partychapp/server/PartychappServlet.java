package com.imjasonh.partychapp.server;

import java.io.IOException;
import java.util.logging.Logger;

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

@SuppressWarnings("serial")
public class PartychappServlet extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(PartychappServlet.class.getName());

  private XMPPService XMPP;

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    super.doPost(req, resp);

    XMPP = XMPPServiceFactory.getXMPPService();

    Message xmppMessage = XMPP.parseMessage(req);
    doXmpp(xmppMessage);
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
