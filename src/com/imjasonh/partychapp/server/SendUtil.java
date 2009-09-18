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
  
  public static boolean getPresence(JID userJID) {
    return XMPP.getPresence(userJID).isAvailable();
  }

  public static void broadcastIncludingSender(String msg, Channel channel, JID serverJID) {
    awakenSnoozers(channel, serverJID);
    JID[] recipients = channel.getMembersJIDsToSendTo();
    sendMessage(msg, serverJID, recipients);
  }

  public static void broadcast(String msg, Channel channel, JID serverJID, JID exclude) {
    awakenSnoozers(channel, serverJID);
    JID[] recipients = channel.getMembersJIDsToSendTo(exclude);
    sendMessage(msg, serverJID, recipients);
  }

  private static void awakenSnoozers(Channel channel, JID serverJID) {
    // awaken snoozers and broadcast them awaking.
    Set<Member> awoken = channel.awakenSnoozers();
    if (!awoken.isEmpty()) {
      channel.put();
      StringBuilder sb = new StringBuilder().append("Members unsnoozed:");
      for (Member m : awoken) {
        sb.append('\n').append(m.getAlias());
      }
      broadcastIncludingSender(sb.toString(), channel, serverJID);
    }
  }

  /**
   * Sends a message, logs unsuccessful sends.
   */
  private static void sendMessage(String msg, JID fromJID, JID... toJIDs) {
    if (toJIDs != null && toJIDs.length > 0) {
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
}
