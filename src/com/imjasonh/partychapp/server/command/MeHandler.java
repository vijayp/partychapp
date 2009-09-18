package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.server.SendUtil;

/**
 * Action taken when the user
 * 
 * @author kushaldave@gmail.com
 */
public class MeHandler extends SlashCommand {

  public MeHandler() {
    super("me");
  }

  public void doCommand(Message msg, String action) {
    assert msg.channel != null;
    assert msg.member != null;

    String broadcast = "_" + msg.member.getAlias() + " " +
        action + "_";
    SendUtil.broadcastIncludingSender(broadcast, msg.channel, msg.serverJID);
  }

  public String documentation() {
    return "/me - describe what you're doing in the third person";
  }
}
