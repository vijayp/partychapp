package com.imjasonh.partychapp.server;

import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.Message;
import com.google.appengine.api.xmpp.XMPPService;
import com.google.appengine.api.xmpp.XMPPServiceFactory;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.User;
import com.imjasonh.partychapp.Message.MessageType;
import com.imjasonh.partychapp.server.command.Command;

import java.io.IOException;
import java.util.Collections;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
    Datastore datastore = Datastore.instance();
    datastore.startRequest();
    
    try {
      JID userJID = jidToLowerCase(xmppMessage.getFromJid());
  
      // should only be "to" one JID, right?
      JID serverJID = jidToLowerCase(xmppMessage.getRecipientJids()[0]);
      String channelName = serverJID.getId().split("@")[0];
  
      String body = xmppMessage.getBody().trim();
  
      if (channelName.equalsIgnoreCase("echo")) {
        handleEcho(xmppMessage);
        return;
      }
  
      Channel channel = datastore.getChannelByName(channelName);
      Member member = null; 
      if (channel != null) {
        member = channel.getMemberByJID(userJID);
      }
      User user = datastore.getOrCreateUser(userJID.getId().split("/")[0]);
      
      com.imjasonh.partychapp.Message message =
        new com.imjasonh.partychapp.Message.Builder()
          .setContent(body)
          .setUserJID(userJID)
          .setServerJID(serverJID)
          .setChannel(channel)
          .setMember(member)
          .setUser(user)
          .setMessageType(MessageType.XMPP)
          .build();
  
      Command.getCommandHandler(message).doCommand(message);
      
      // {@link User#fixUp} can't be called by {@link FixingDatastore}, since
      // it can't know what channel the user is currently messaging, so we have
      // to do it ourselves.
      user.fixUp(message.channel);
      user.maybeMarkAsSeen();
    } finally {    
      datastore.endRequest();
    }
  }

  private void handleEcho(Message xmppMessage) {
    logger.severe("Body of message sent to echo@ is: " + xmppMessage.getBody());
    SendUtil.sendMessage(
        "echo: " + xmppMessage.getBody(),
        xmppMessage.getRecipientJids()[0],
        Collections.singletonList(xmppMessage.getFromJid()));
  }
}
