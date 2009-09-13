package com.imjasonh.partychapp.server.command;

import java.util.regex.Pattern;

import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.server.SendUtil;

public class AliasHandler implements CommandHandler {
  public static Pattern pattern = Pattern.compile("^/(alias|rename) [a-zA-Z0-9\\-_'\\*]+");
	
  public void doCommand(Message msg) {
    String oldAlias = msg.member.getAlias();
    String alias = msg.content.replace("/alias ", ""); // TODO do this with matcher

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

  public boolean matches(Message msg) {
	  return pattern.matcher(msg.content.trim()).matches();
  }
  
  public String documentation() {
	  return "/alias - rename yourself";
  }
}
