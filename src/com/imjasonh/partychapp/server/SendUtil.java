package com.imjasonh.partychapp.server;

import java.util.Set;
import java.util.logging.Logger;

import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.MessageBuilder;
import com.google.appengine.api.xmpp.SendResponse;
import com.google.appengine.api.xmpp.XMPPService;
import com.google.appengine.api.xmpp.XMPPServiceFactory;
import com.google.appengine.api.xmpp.SendResponse.Status;
import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Member;

public abstract class SendUtil {
  private static XMPPService XMPP = XMPPServiceFactory.getXMPPService();

  private static final Logger LOG = Logger.getLogger(Channel.class.getName());

  public static void setXMPP(XMPPService xmpp) {
    XMPP = xmpp;
  }

  public static void sendDirect(String msg, JID userJID, JID serverJID) {
    sendMessage(msg, serverJID, userJID);
  }

  private static void broadcastHelper(String msg, Channel channel, JID userJID, JID serverJID,
      JID toExclude) {
    // awaken snoozers and broadcast them awaking.
    Set<Member> awoken = channel.awakenSnoozers();
    if (!awoken.isEmpty()) {
      channel.put();
      StringBuilder sb = new StringBuilder().append("Members unsnoozed:");
      for (Member m : awoken) {
        sb.append('\n').append(m.getAlias());
      }
      broadcast(sb.toString(), channel, userJID, serverJID);
    }

    JID[] recipients = channel.getMembersJIDsToSendTo(toExclude);

    sendMessage(msg, serverJID, recipients);
  }

  /**
   * Sends a message, logs unsuccessful sends.
   */
  private static void sendMessage(String msg, JID fromJID, JID... toJIDs) {
    if (toJIDs.length > 0) {
      SendResponse response = XMPP.sendMessage(new MessageBuilder()
          .withBody(msg)
          .withFromJid(fromJID)
          .withRecipientJids(toJIDs)
          .build());

      if (response == null) {
        LOG.severe("XMPP.sendMessage() response is null!");
      } else {
        for (JID jid : toJIDs) {
          Status status = response.getStatusMap().get(jid);
          if (status != Status.SUCCESS) {
            StringBuilder sb = new StringBuilder().append("sendMessage unsuccessful! ")
                .append("status: ")
                .append(status.name())
                .append(" from: ")
                .append(fromJID.getId())
                .append(" / to: ")
                .append(jid.getId());
            LOG.severe(sb.toString());
          }
        }
      }
    }
  }

  public static void broadcastIncludingSender(String msg, Channel channel, JID userJID,
      JID serverJID) {
    broadcastHelper(msg, channel, userJID, serverJID, null);
  }

  public static void broadcast(String msg, Channel channel, JID userJID, JID serverJID) {
    broadcastHelper(msg, channel, userJID, serverJID, userJID);
  }
}
