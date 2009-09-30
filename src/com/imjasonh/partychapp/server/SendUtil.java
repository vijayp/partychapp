package com.imjasonh.partychapp.server;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.MessageBuilder;
import com.google.appengine.api.xmpp.SendResponse;
import com.google.appengine.api.xmpp.XMPPService;
import com.google.appengine.api.xmpp.XMPPServiceFactory;
import com.google.appengine.api.xmpp.SendResponse.Status;
import com.imjasonh.partychapp.Channel;

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

  public static void invite(String email, JID serverJID) {
    XMPP.sendInvitation(new JID(email), serverJID);
  }

  /**
   * Sends a message, logs unsuccessful sends.
   */
  public static void sendMessage(String msg, JID fromJID, JID... toJIDs) {
    if (!fromJID.getId().contains("appspotchat.com")) {
      throw new RuntimeException(fromJID
        + " is not a server JID but is being used as the from");
    }

    if (toJIDs == null || toJIDs.length == 0) {
      return;
    }

    SendResponse response = null;
    try {
      response =
        XMPP.sendMessage(new MessageBuilder().withBody(msg)
                .withFromJid(fromJID).withRecipientJids(toJIDs).build());
    } catch (RuntimeException e) {
      LOG.log(Level.SEVERE, "Got exception while sending msg '" + msg
        + "' from " + fromJID + " to " + Arrays.toString(toJIDs), e);
      return;
    }

    if (response == null) {
      LOG.severe("XMPP.sendMessage() response is null!");
      return;
    }

    for (JID jid : toJIDs) {
      Status status = response.getStatusMap().get(jid);
      if (status != Status.SUCCESS) {
        StringBuilder sb =
          new StringBuilder().append("sendMessage unsuccessful! ")
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
