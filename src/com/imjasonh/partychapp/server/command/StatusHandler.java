package com.imjasonh.partychapp.server.command;
import javax.servlet.http.HttpServletResponse;

import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.User;
import com.imjasonh.partychapp.DebuggingOptions.Option;

public class StatusHandler extends SlashCommand {
  
  public StatusHandler() {
    super("status");
  }

  @Override
  public void doCommand(Message msg, String argument, HttpServletResponse resp) {
    String reply = "You are currently in '" + msg.channel.getName() + "' as '" + msg.member.getAlias() + ".'";
    User u = msg.user;
    if (u.phoneNumber() != null) {
      reply += " Your phone number is " + u.phoneNumber() + ".";
    }
    if (u.carrier() != null) {
      reply += " Your carrier is " + u.carrier().shortName + ".";
    }
    if (msg.member.debugOptions().isEnabled(Option.SEQUENCE_IDS)) {
      reply += "\nCurrent sequence ID: " + msg.channel.getSequenceId();
    }
    msg.channel.sendDirect(reply, msg.member, resp);
  }
  
  public String documentation() {
    return "/status - show what room you're in";
  }
}
