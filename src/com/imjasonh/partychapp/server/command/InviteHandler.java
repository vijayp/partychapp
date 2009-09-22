package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.server.SendUtil;

/**
 * Action taken when the user
 * 
 * @author kushaldave@gmail.com
 */
public class InviteHandler extends SlashCommand {

  public InviteHandler() {
    super("invite");
  }

  public void doCommand(Message msg, String action) {
    assert msg.channel != null;
    assert msg.member != null;
    
    // Add undo support?
    // msg.member.addToLastMessages(msg.content);
    SendUtil.invite(action, msg.serverJID);
    msg.channel.invite(action);
    msg.channel.put();
    
    String broadcast = "_" + msg.member.getAlias() + " invited " +
        action + "_";
    SendUtil.broadcastIncludingSender(broadcast, msg.channel, msg.serverJID);
  }

  public String documentation() {
    return "/invite - Invite an email address to this room";
  }
}
