package com.imjasonh.partychapp.server.command;

import java.util.regex.Matcher;

import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.server.SendUtil;

/**
 * Action taken when the user 
 * 
 * @author kushaldave@gmail.com
 */
public class MeHandler extends SlashCommand {

  public MeHandler() {
    super("me (.+)");
  }
  
  public void doCommand(Message msg) {
    assert msg.channel != null;
    assert msg.member != null;

    Matcher matcher = getMatcher(msg);
    matcher.matches();
    String broadcast = "_" + msg.member.getAlias() + " " +
      matcher.group(1) + "_";
    SendUtil.broadcastIncludingSender(broadcast, msg.channel, null, msg.serverJID);
  }

  public String documentation() {
    return "/me - describe what you're doing in the third person";
  }
}
