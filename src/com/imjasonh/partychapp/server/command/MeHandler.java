package com.imjasonh.partychapp.server.command;

import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.imjasonh.partychapp.Message;

/**
 * Action taken when the user
 * 
 * @author kushaldave@gmail.com
 */
public class MeHandler extends SlashCommand {

  public MeHandler() {
    super("me");
  }

  @Override
  public void doCommand(Message msg, String action, HttpServletResponse resp) {
    assert msg.channel != null;
    assert msg.member != null;
    
    if (Strings.isNullOrEmpty(action)) {
      msg.channel.sendDirect("You must specify an action.", msg.member, resp);
      return;
    }
    
    msg.member.addToLastMessages(msg.content);
    msg.channel.put();

    String broadcast = "_" + msg.member.getAlias() + " " +
        action + "_";
    msg.channel.broadcastIncludingSender(broadcast, resp);
  }

  public String documentation() {
    return "/me - describe what you're doing in the third person";
  }
}
