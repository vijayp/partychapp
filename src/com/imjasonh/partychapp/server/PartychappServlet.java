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
import com.google.common.collect.Lists;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Message.MessageType;
import com.imjasonh.partychapp.server.command.Command;

@SuppressWarnings("serial")
public class PartychappServlet extends HttpServlet {
  private static final Logger logger =
      Logger.getLogger(PartychappServlet.class.getName());

  private XMPPService XMPP;

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    super.doPost(req, resp);

    XMPP = XMPPServiceFactory.getXMPPService();

    Message xmppMessage = null;
    try {
      xmppMessage = XMPP.parseMessage(req);
    } catch (IllegalArgumentException e) {
      // These exceptions are apparently caused by a bug in the gtalk flash
      // gadget, so let's just ignore them.
      // http://code.google.com/p/googleappengine/issues/detail?id=2082
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    doXmpp(xmppMessage);
    
    resp.setStatus(HttpServletResponse.SC_OK);
  }

  JID jidToLowerCase(JID in) {
    return new JID(in.getId().toLowerCase());
  }
  
  public void doXmpp(Message xmppMessage) {
    Datastore.instance().startRequest();
    
    JID userJID = jidToLowerCase(xmppMessage.getFromJid());

    // should only be "to" one JID, right?
    JID serverJID = jidToLowerCase(xmppMessage.getRecipientJids()[0]);
    String channelName = serverJID.getId().split("@")[0];

    String body = xmppMessage.getBody().trim();

    com.imjasonh.partychapp.Message message =
        new com.imjasonh.partychapp.Message(
            body, userJID, serverJID, null, null, null, MessageType.XMPP);

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
    logger.severe("Body of message sent to echo@ is: " + message.content);
    SendUtil.sendMessage("echo: " + message.content,
                         message.serverJID,
                         Lists.newArrayList(message.userJID));
  }
}
