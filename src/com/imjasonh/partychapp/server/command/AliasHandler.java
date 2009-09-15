package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.server.SendUtil;

public class AliasHandler extends SlashCommand {
  
  AliasHandler() {
    super("(alias|rename) [a-zA-Z0-9\\-_'\\*]+");
  }

  public void doCommand(Message msg) {
    String oldAlias = msg.member.getAlias();
    String alias = getMatcher(msg).group(1);

    for (Member m : msg.channel.getMembers()) {
      if (m.getAlias().equals(alias)) {
        String reply = "That alias is already taken";
        SendUtil.broadcast(reply, msg.channel, msg.userJID, msg.serverJID);
        return;
      }
    }

    msg.member.setAlias(alias);
    msg.channel.put();
    
    String youMsg = "You are now known as '" + alias + "'";
    SendUtil.sendDirect(youMsg, msg.userJID, msg.serverJID);

    String reply = "'" + oldAlias + "' is now known as '" + alias + "'";
    SendUtil.broadcast(reply, msg.channel, msg.userJID, msg.serverJID);
  }
  
  public String documentation() {
	  return "/alias - rename yourself";
  }
}
