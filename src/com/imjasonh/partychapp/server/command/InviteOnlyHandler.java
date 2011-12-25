package com.imjasonh.partychapp.server.command;

import javax.servlet.http.HttpServletResponse;

import com.imjasonh.partychapp.Message;

/**
 * Make a room invite-only.
 * 
 * @author kushaldave@gmail.com
 */
public class InviteOnlyHandler extends SlashCommand {

  public InviteOnlyHandler() {
    super("inviteonly");
  }

  @Override
  public void doCommand(Message msg, String action, HttpServletResponse resp) {
    assert msg.channel != null;
    assert msg.member != null;
    
    msg.channel.setInviteOnly(!msg.channel.isInviteOnly());
    msg.channel.put();
    
    String broadcast = "_" + msg.member.getAlias() + " set the room to " +
        (msg.channel.isInviteOnly() ? "" : "not ") + "invite-only._";
    msg.channel.broadcastIncludingSender(broadcast, resp);
  }

  public String documentation() {
    return "/inviteonly - Set the room to invite-only";
  }
}
