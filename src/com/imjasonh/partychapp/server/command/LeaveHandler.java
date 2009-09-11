package com.imjasonh.partychapp.server.command;

import com.google.appengine.api.xmpp.JID;
import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.server.SendUtil;

public class LeaveHandler implements CommandHandler {

  public void doCommand(String content, JID userJID, JID serverJID, Member member, Channel channel) {
    channel.removeMember(member);
    channel.put();
    String youMsg = "You have left the room '" + channel.getName() + "'";
    SendUtil.sendDirect(youMsg, userJID, serverJID);

    String msg = member.getAlias() + " has left the room (" + member.getJID() + ")";
    SendUtil.broadcast(msg, channel, userJID, serverJID);
  }
}
