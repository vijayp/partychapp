package com.imjasonh.partychapp.server;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.MessageBuilder;
import com.google.appengine.api.xmpp.SendResponse;
import com.google.appengine.api.xmpp.XMPPFailureException;
import com.google.appengine.api.xmpp.XMPPService;
import com.google.appengine.api.xmpp.XMPPServiceFactory;
import com.google.appengine.api.xmpp.SendResponse.Status;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.ChannelStats;
import com.imjasonh.partychapp.Configuration;

public abstract class SendUtil {
  private static XMPPService XMPP = XMPPServiceFactory.getXMPPService();

  private static final Logger LOG = Logger.getLogger(Channel.class.getName());

  public static void setXMPP(XMPPService xmpp) {
    XMPP = xmpp;
  }

  public static void sendDirect(String msg, JID userJID, JID serverJID) {
    sendMessage(msg, serverJID, Lists.newArrayList(userJID));
  }

  public static boolean getPresence(JID userJID, JID serverJID) {
    try {
      return XMPP.getPresence(userJID, serverJID).isAvailable();
    } catch (XMPPFailureException e) {
      LOG.log(Level.WARNING, "got exception while getting presence for " + userJID, e);
      return false;
    }
  }

  /**
   * If inviting from the web or XMPP UIs, use {@link InviteUtil#invite} (which
   * sends both XMPP and email invitations).
   */
  public static void invite(String email, JID serverJID) {
    XMPP.sendInvitation(new JID(email), serverJID);
  }

  /**
   * Sends a message. Unsuccessful sends are logged, and the JIDs they were 
   * meant for are returned.
   */
  public static Set<JID> sendMessage(String msg, JID fromJID, List<JID> toJIDs) {
    if (!fromJID.getId().contains(Configuration.chatDomain)) {
      throw new RuntimeException(fromJID
        + " is not a server JID but is being used as the from");
    }

    if (toJIDs == null || toJIDs.isEmpty()) {
      return Collections.emptySet();
    }
    
    ChannelStats.increment(fromJID, msg, toJIDs);

    SendResponse response = null;
    try {
      response =
        XMPP.sendMessage(new MessageBuilder().withBody(msg)
                .withFromJid(fromJID)
                .withRecipientJids(toJIDs.toArray(new JID[]{}))
                .build());
    } catch (RuntimeException e) {
      LOG.log(Level.SEVERE, "Got exception while sending msg '" + msg
        + "' from " + fromJID + " to " + toJIDs.toString(), e);
      return Collections.emptySet();
    }

    if (response == null) {
      LOG.severe("XMPP.sendMessage() response is null!");
      return Collections.emptySet();
    }

    Set<JID> errorJIDs = Sets.newHashSet();
    for (JID jid : toJIDs) {
      Status status = response.getStatusMap().get(jid);
      if (status != Status.SUCCESS) {
        errorJIDs.add(jid);
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
    
    return errorJIDs;
  }
}
