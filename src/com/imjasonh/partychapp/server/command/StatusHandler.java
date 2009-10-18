package com.imjasonh.partychapp.server.command;
import com.imjasonh.partychapp.Message;

public class StatusHandler extends SlashCommand {
  
  public StatusHandler() {
    super("status");
  }
        
  public void doCommand(Message msg, String argument) {
    String reply = "You are currently in '" + msg.channel.getName() + "' as '" + msg.member.getAlias() + "'";
    msg.channel.sendDirect(reply, msg.member);
  }
  
  public String documentation() {
    return "/status - show what room you're in";
  }
}
