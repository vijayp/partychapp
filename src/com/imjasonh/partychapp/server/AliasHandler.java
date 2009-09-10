package com.imjasonh.partychapp.server;

import com.google.appengine.api.xmpp.JID;
import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Member;

public class AliasHandler extends CommandHandler {

  @Override
  void doCommand(String content, JID userJID, JID serverJID, Member member, Channel channel) {
    String oldAlias = member.getAlias();
    String alias = content.replace("/alias ", ""); // TODO do this with matcher

    for (Member m : channel.getMembers()) {
      if (m.getAlias().equals(alias)) {
        String msg = "That alias is already taken";
        broadcast(msg, channel, userJID, serverJID);
        return;
      }
    }

    member.setAlias(alias);
    channel.put();

    String youMsg = "You are now known as '" + alias + "'";
    sendDirect(youMsg, userJID, serverJID);

    String msg = "'" + oldAlias + "' is now known as '" + alias + "'";
    broadcast(msg, channel, userJID, serverJID);
  }

}
