package com.imjasonh.partychapp.server.command;

import javax.servlet.http.HttpServletResponse;

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
  public void doCommand(Message msg, String action, HttpServletResponse resp) {
    assert msg.channel != null;
    assert msg.member != null;
    
    if (Strings.isNullOrEmpty(action)) {
      msg.channel.sendDirect("You must specify someone to kick", msg.member, resp);
      return;
    }
    
    if (msg.channel.kick(action)) {
      msg.channel.put();
      String broadcast = "_" + msg.member.getAlias() + " kicked " +
          action + "_";
      msg.channel.broadcastIncludingSender(broadcast, resp);
    } else {
      msg.channel.sendDirect("No such member", msg.member, resp);
    }
  }

  public String documentation() {
    return "/kick - Remove a user or invitation from this room";
  }
}
