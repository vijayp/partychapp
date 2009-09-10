package com.imjasonh.partychapp.server;

import java.util.Set;

import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.MessageBuilder;
import com.google.appengine.api.xmpp.XMPPService;
import com.google.appengine.api.xmpp.XMPPServiceFactory;
import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Member;

public abstract class CommandHandler {

  static final XMPPService XMPP = XMPPServiceFactory.getXMPPService();

  static void sendDirect(String msg, JID userJID, JID serverJID) {
    XMPP.sendMessage(new MessageBuilder()
        .withBody(msg)
        .withFromJid(serverJID)
        .withRecipientJids(userJID)
        .build());
  }

  static void broadcast(String msg, Channel channel, JID userJID, JID serverJID) {
    if (channel.getMembers().size() > 1) {

      // awaken snoozers and broadcast them awawking.
      Set<Member> awoken = channel.awakenSnoozers();
      if (!awoken.isEmpty()) {
        channel.put();
        StringBuilder sb = new StringBuilder().append("Members awoken:").append('\n');
        for (Member m : awoken) {
          sb.append('\n').append(m.getAlias());
        }
        broadcast(sb.toString(), channel, userJID, serverJID);
      }

      JID[] recipients = channel.getMembersJIDsToSendTo(userJID);
      XMPP.sendMessage(new MessageBuilder()
          .withBody(msg)
          .withFromJid(serverJID)
          .withRecipientJids(recipients)
          .build());
    }
  }

  abstract void doCommand(String content, JID userJID, JID serverJID, Member member, Channel channel);
}
