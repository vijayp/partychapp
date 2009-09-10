package com.imjasonh.partychapp.server;

import com.google.appengine.api.xmpp.JID;
import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Member;

public class LeaveHandler extends CommandHandler {

  @Override
  void doCommand(String content, JID userJID, JID serverJID, Member member, Channel channel) {
    channel.removeMember(member);
    channel.put();
    String youMsg = "You have left the room '" + channel.getName() + "'";
    sendDirect(youMsg, userJID, serverJID);

    String msg = member.getAlias() + " has left the room (" + member.getJID() + ")";
    broadcast(msg, channel, userJID, serverJID);
  }
}
