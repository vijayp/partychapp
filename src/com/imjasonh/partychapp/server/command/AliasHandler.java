package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.server.SendUtil;

public class AliasHandler extends SlashCommand {
  
  private static final String ALIAS_REGEX = "[a-zA-Z0-9\\-_'\\*]+";
  
  AliasHandler() {
    super("alias", "rename");
  }

  @Override
  public void doCommand(Message msg, String alias) {
    String oldAlias = msg.member.getAlias();
    if (alias == null || !alias.matches(ALIAS_REGEX)) {
      String reply = "That alias contains invalid characters";
      SendUtil.sendDirect(reply, msg.userJID, msg.serverJID);
      return;
    }

    for (Member m : msg.channel.getMembers()) {
      if (m.getAlias().equals(alias)) {
        String reply = "That alias is already taken";
        SendUtil.sendDirect(reply, msg.userJID, msg.serverJID);
        return;
      }
    }

    msg.member.setAlias(alias);
    msg.channel.put();
    
    String youMsg = "You are now known as '" + alias + "'";
    SendUtil.sendDirect(youMsg, msg.userJID, msg.serverJID);

    String reply = "'" + oldAlias + "' is now known as '" + alias + "'";
    SendUtil.broadcastIncludingSender(reply, msg.channel, msg.serverJID);
  }
  
  public String documentation() {
	  return "/alias - rename yourself";
  }
}
