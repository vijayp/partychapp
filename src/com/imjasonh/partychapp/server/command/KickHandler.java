package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.server.SendUtil;

/**
 * Action taken when the user
 * 
 * @author kushaldave@gmail.com
 */
public class KickHandler extends SlashCommand {

  public KickHandler() {
    super("kick");
  }

  public void doCommand(Message msg, String action) {
    assert msg.channel != null;
    assert msg.member != null;
    
    if (msg.channel.kick(action)) {
      String broadcast = "_" + msg.member.getAlias() + " kicked " +
          action + "_";
      SendUtil.broadcastIncludingSender(broadcast, msg.channel, msg.serverJID);
    } else {
      SendUtil.sendDirect("No such member", msg.userJID, msg.serverJID);
    }
  }

  public String documentation() {
    return "/kick - Remove a user or invitation from this room";
  }
}
