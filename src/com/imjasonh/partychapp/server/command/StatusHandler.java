package com.imjasonh.partychapp.server.command;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.User;

public class StatusHandler extends SlashCommand {
  
  public StatusHandler() {
    super("status");
  }
        
  public void doCommand(Message msg, String argument) {
    String reply = "You are currently in '" + msg.channel.getName() + "' as '" + msg.member.getAlias() + ".'";
    User u = msg.member.user();
    System.err.println(u);
    if (u.phoneNumber() != null) {
      reply += " Your phone number is " + u.phoneNumber() + ".";
    }
    if (u.carrier() != null) {
      reply += " Your carrier is " + u.carrier().shortName + ".";
    }
    msg.channel.sendDirect(reply, msg.member);
  }
  
  public String documentation() {
    return "/status - show what room you're in";
  }
}
