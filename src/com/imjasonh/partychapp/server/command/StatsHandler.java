package com.imjasonh.partychapp.server.command;

import java.text.DateFormat;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Message;

public class StatsHandler extends SlashCommand {
  public StatsHandler() {
    super("stats");
  }
  
  private static final DateFormat df =
    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
  
  public void doCommand(Message msg, String argument) {
    Datastore.Stats stats = Datastore.instance().getStats();
    String reply = "Number of channels (as of " + df.format(stats.timestamp) + "): " + stats.numChannels + "\n";
    reply += "1-day active users: " + stats.oneDayActiveUsers + "\n";
    reply += "7-day active users: " + stats.sevenDayActiveUsers + "\n";
    reply += "Number of users: " + stats.numUsers + "\n";
    // TODO(nsanch): uncomment when we've had the User object for more than
    // 30 days (mid-January)
    // reply += "30-day active users: " + stats.thirtyDayActiveUsers + "\n";
    
    msg.channel.sendDirect(reply, msg.member);
  }

  public String documentation() {
    return "/stats - return system stats";
  }
}
