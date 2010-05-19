package com.imjasonh.partychapp.server.command;

import com.google.common.base.Strings;

import com.imjasonh.partychapp.Message;

/**
 * Allows current members to remove other members, invitees, or those who have
 * requested invitations.
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
    
    if (Strings.isNullOrEmpty(action)) {
      msg.channel.sendDirect("You must specify someone to kick", msg.member);
      return;
    }
    
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
