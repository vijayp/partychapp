package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Message;

public class StatsHandler extends SlashCommand {
  public StatsHandler() {
    super("stats");
  }
  
  @Override
  public void doCommand(Message msg, String argument) {
    Datastore.Stats stats = Datastore.instance().getStats();    
    msg.channel.sendDirect(stats.toString(), msg.member);
  }

  public String documentation() {
    return "/stats - return system stats";
  }
}
