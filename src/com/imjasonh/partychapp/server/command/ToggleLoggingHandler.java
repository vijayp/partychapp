package com.imjasonh.partychapp.server.command;

import javax.servlet.http.HttpServletResponse;

import com.imjasonh.partychapp.Message;

/**
 * Toggle the logging setting for a room.
 * 
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class ToggleLoggingHandler extends SlashCommand {

  public ToggleLoggingHandler() {
    super("togglelogging", "toggle-logging", "toggle_logging");
  }

  @Override
  public void doCommand(Message msg, String action, HttpServletResponse resp) {
    assert msg.channel != null;
    assert msg.member != null;
    
    msg.channel.setLoggingDisabled(!msg.channel.isLoggingDisabled());
    msg.channel.put();
    
    String broadcast = "_" + msg.member.getAlias() + " has " +
        (msg.channel.isLoggingDisabled() ? "disabled" : "enabled") +
        " logging._";
    msg.channel.broadcastIncludingSender(broadcast, resp);
  }

  public String documentation() {
    return "/togglelogging - Enable or disable logging for a room.";
  }
}
