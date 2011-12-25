package com.imjasonh.partychapp.server.command;

import javax.servlet.http.HttpServletResponse;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Message;

public class StatsHandler extends SlashCommand {
  public StatsHandler() {
    super("stats");
  }
  
  @Override
  public void doCommand(Message msg, String argument, HttpServletResponse resp) {
    Datastore.Stats stats = Datastore.instance().getStats(false);    
    msg.channel.sendDirect(stats.toString(), msg.member, resp);
  }

  public String documentation() {
    return "/stats - return system stats";
  }
}
