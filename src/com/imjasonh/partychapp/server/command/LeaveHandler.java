package com.imjasonh.partychapp.server.command;

import java.util.regex.Pattern;

import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.server.SendUtil;

public class LeaveHandler implements CommandHandler {
  public static final Pattern pattern = Pattern.compile("^/(leave|exit)");

  public void doCommand(Message msg) {
    msg.channel.removeMember(msg.member);
    msg.channel.put();
    String youMsg = "You have left the room '" + msg.channel.getName() + "'";
    SendUtil.sendDirect(youMsg, msg.userJID, msg.serverJID);

    String reply = msg.member.getAlias() + " has left the room (" + msg.member.getJID() + ")";
    SendUtil.broadcast(reply, msg.channel, msg.userJID, msg.serverJID);
  }
  
  public boolean matches(Message msg) {
	  return pattern.matcher(msg.content.trim()).matches();
  }
  
  public String documentation() {
	  return "/leave - leave the room";
  }
}
