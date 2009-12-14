package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Message;

public class StatsHandler extends SlashCommand {
  public StatsHandler() {
    super("stats");
  }
  
  public void doCommand(Message msg, String argument) {
    Datastore.Stats stats = Datastore.instance().getStats();
    String reply = "Number of channels: " + stats.numChannels + "\n";
    reply += "Number of users: " + stats.numUsers + "\n";
    reply += "1-day active users: " + stats.numUsers + "\n";
    reply += "1-day active users: " + stats.oneDayActiveUsers + "\n";
    reply += "7-day active users: " + stats.sevenDayActiveUsers + "\n";
    reply += "30-day active users: " + stats.thirtyDayActiveUsers + "\n";
    reply += "Some stats were last refreshed at: " + stats.timestamp;
    
    msg.channel.sendDirect(reply, msg.member);
  }

  public String documentation() {
    return "/stats - return system stats";
  }
}
