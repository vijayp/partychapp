package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.server.SendUtil;

public class LeaveHandler extends SlashCommand {
  
  LeaveHandler() {
    super("(leave|exit)");
  }

  public void doCommand(Message msg) {
    msg.channel.removeMember(msg.member);
    msg.channel.put();
    String youMsg = "You have left the room '" + msg.channel.getName() + "'";
    SendUtil.sendDirect(youMsg, msg.userJID, msg.serverJID);

    String reply = msg.member.getAlias() + " has left the room (" + msg.member.getJID() + ")";
    SendUtil.broadcast(reply, msg.channel, msg.userJID, msg.serverJID);
  }
  
  public String documentation() {
	  return "/leave - leave the room";
  }
}
