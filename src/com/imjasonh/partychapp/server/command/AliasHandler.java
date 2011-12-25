package com.imjasonh.partychapp.server.command;

import javax.servlet.http.HttpServletResponse;

import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.Message;

public class AliasHandler extends SlashCommand {
  // Letters, symbols, numbers and some punctuation
  public static final String ALIAS_REGEX = "[\\pL\\pS\\pN\\-_'\\*.]+";
  
  AliasHandler() {
    super("alias", "rename", "nick");
  }

  @Override
  public void doCommand(Message msg, String alias, HttpServletResponse resp) {
    if (alias == null || !alias.matches(ALIAS_REGEX)) {
      String reply = "That alias contains invalid characters";
      msg.channel.sendDirect(reply, msg.member, resp);
      return;
    }

    for (Member m : msg.channel.getMembers()) {
      if (m.getAlias().equals(alias)) {
        String reply = "That alias is already taken";
        msg.channel.sendDirect(reply, msg.member, resp);
        return;
      }
    }

    String oldAlias = msg.member.getAlias();
    msg.member.setAlias(alias);
    msg.channel.put();
    
    String youMsg = "You are now known as '" + alias + "'";
    msg.channel.sendDirect(youMsg, msg.member, resp);

    String reply = "'" + oldAlias + "' is now known as '" + alias + "'";
    msg.channel.broadcastIncludingSender(reply, resp);
  }
  
  public String documentation() {
    return "/alias name - rename yourself to 'name'";
  }
}
