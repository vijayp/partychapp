package com.imjasonh.partychapp.server;

import java.util.Set;
import java.util.logging.Logger;

import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.MessageBuilder;
import com.google.appengine.api.xmpp.XMPPService;
import com.google.appengine.api.xmpp.XMPPServiceFactory;
import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Member;

public abstract class SendUtil {

  private static final XMPPService XMPP = XMPPServiceFactory.getXMPPService();

  private static final Logger LOG = Logger.getLogger(Channel.class.getName());

  public static void sendDirect(String msg, JID userJID, JID serverJID) {
    XMPP.sendMessage(new MessageBuilder()
        .withBody(msg)
        .withFromJid(serverJID)
        .withRecipientJids(userJID)
        .build());
  }

  public static void broadcast(String msg, Channel channel, JID userJID, JID serverJID) {
    // awaken snoozers and broadcast them awawking.
    Set<Member> awoken = channel.awakenSnoozers();
    if (!awoken.isEmpty()) {
      channel.put();
      StringBuilder sb = new StringBuilder().append("Members unsnoozed:");
      for (Member m : awoken) {
        sb.append('\n').append(m.getAlias());
      }
      broadcast(sb.toString(), channel, userJID, serverJID);
    }

    JID[] recipients = channel.getMembersJIDsToSendTo(userJID);

    LOG.severe("message: " + msg +
        ", fromjid: " + serverJID +
        ", tojid: " + recipients);

    if (recipients.length > 0) {
      XMPP.sendMessage(new MessageBuilder()
          .withBody(msg)
          .withFromJid(serverJID)
          .withRecipientJids(recipients)
          .build());
    }
  }

}
