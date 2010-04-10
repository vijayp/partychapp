package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;

/**
 * Action taken when the user
 * 
 * @author kushaldave@gmail.com
 */
public class KickHandler extends SlashCommand {

  public KickHandler() {
    super("kick");
  }

  @Override
  public void doCommand(Message msg, String action) {
    assert msg.channel != null;
    assert msg.member != null;
    
    if (msg.channel.kick(action)) {
      String broadcast = "_" + msg.member.getAlias() + " kicked " +
          action + "_";
      msg.channel.broadcastIncludingSender(broadcast);
    } else {
      msg.channel.sendDirect("No such member", msg.member);
    }
  }

  public String documentation() {
    return "/kick - Remove a user or invitation from this room";
  }
}
