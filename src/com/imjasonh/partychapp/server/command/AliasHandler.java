package com.imjasonh.partychapp.server.command;

import com.google.appengine.api.xmpp.JID;
import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.server.SendUtil;

public class AliasHandler implements CommandHandler {

  @Override
  public void doCommand(String content, JID userJID, JID serverJID, Member member, Channel channel) {
    String oldAlias = member.getAlias();
    String alias = content.replace("/alias ", ""); // TODO do this with matcher

    for (Member m : channel.getMembers()) {
      if (m.getAlias().equals(alias)) {
        String msg = "That alias is already taken";
        SendUtil.broadcast(msg, channel, userJID, serverJID);
        return;
      }
    }

    member.setAlias(alias);
    channel.put();

    String youMsg = "You are now known as '" + alias + "'";
    SendUtil.sendDirect(youMsg, userJID, serverJID);

    String msg = "'" + oldAlias + "' is now known as '" + alias + "'";
    SendUtil.broadcast(msg, channel, userJID, serverJID);
  }

}
