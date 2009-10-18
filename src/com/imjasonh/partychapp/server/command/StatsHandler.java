package com.imjasonh.partychapp.server.command;

import java.util.Map;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Message;

public class StatsHandler extends SlashCommand {
  public StatsHandler() {
    super("stats");
  }
  
  public int getOrZero(Map<String, Integer> map, String key) {
    return map.containsKey(key) ? map.get(key) : 0; 
  }
  
  public void doCommand(Message msg, String argument) {
    Map<String, Integer> stats = Datastore.instance().getStats();
    String reply = "Number of channels: " + getOrZero(stats, "Channel");
    //     "Number of members: " + getOrZero(stats, "Member");
    msg.channel.sendDirect(reply, msg.member);
  }

  public String documentation() {
    return "/stats - return system stats";
  }
}
