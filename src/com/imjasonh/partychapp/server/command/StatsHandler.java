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
    reply += "Number of users (it's actually more than this): " + stats.numUsers + "\n";
    reply += "Stats last refreshed: " + stats.timestamp;
    //     "Number of members: " + getOrZero(stats, "Member");
    msg.channel.sendDirect(reply, msg.member);
  }

  public String documentation() {
    return "/stats - return system stats";
  }
}
