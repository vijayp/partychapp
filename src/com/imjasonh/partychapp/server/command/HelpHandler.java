package com.imjasonh.partychapp.server.command;

import java.util.regex.Pattern;

import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.server.SendUtil;

public class HelpHandler implements CommandHandler {
  private static Pattern pattern = Pattern.compile("^/(help|commands)");
	
  public void doCommand(Message msg) {
    StringBuilder sb = new StringBuilder().append("List of commands:").append('\n');
    for (Command command : Command.values()) {
      sb.append("* ")
          .append(command.commandHandler.documentation())
          .append('\n');
    }
    sb.append("* Message echo@ to hear yourself talk").append('\n');

    SendUtil.sendDirect(sb.toString(), msg.userJID, msg.serverJID);
  }
  
  public boolean matches(Message msg) {
	  return pattern.matcher(msg.content.trim()).matches();
  }
  
  public String documentation() {
	return "/help shows this";
  }
}
