package com.imjasonh.partychapp.server.command;

import java.text.DateFormat;
import java.util.Date;

import com.imjasonh.partychapp.Message;

public class SnoozeHandler extends SlashCommand {
  public SnoozeHandler() {
    super("snooze");
  }
  
  private Long timeInMillisForTesting = null;
  private DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);
  
  public void setTimeForTesting(long timeInMillis) {
    this.timeInMillisForTesting = timeInMillis;
  }
  
  public long now() {
    if (timeInMillisForTesting == null) {
      return System.currentTimeMillis();
    }
    return timeInMillisForTesting;
  }

  @Override
  void doCommand(Message msg, String argument) {
    argument = argument.trim();
    char unit = argument.charAt(argument.length() - 1);
    int num = 0;
    String usage = "You must specify a number and a unit of time to snooze " +
      "for -- for example, 20s for 20 seconds, 45m for 45 minutes, 1h for 1 hour," +
      "or 2d for 2 days.";
    try {
      num = Integer.valueOf(argument.substring(0, argument.length() - 1));
    } catch (NumberFormatException e) {
      msg.channel.sendDirect("Sorry, couldn't understand the time period you asked for. " + usage,
                             msg.member);
      return;
    }
    if (num < 0) {
      msg.channel.sendDirect("You can't snooze for a negative number of seconds! " + usage,
                             msg.member);
      return;
    }

    String unitToPrint = "";
    int seconds = 0;
    switch (unit) {
    case 's':
      unitToPrint = "seconds";
      seconds = num;
      break;
    case 'm':
      unitToPrint = "minutes";
      seconds = num*60;
      break;
    case 'h':
      unitToPrint = "hours";
      seconds = num*60*60;
      break;
    case 'd':
      unitToPrint = "days";
      seconds = num*60*60*24;
      break;
    default:
      msg.channel.sendDirect(usage, msg.member);
      return;
    }

    msg.member.setSnoozeUntil(new Date(now() + 1000*seconds));
    String reply = "Okay, snoozing for " + num + " " + unitToPrint +
        " (" + seconds + " seconds), until " +
        df.format(msg.member.getSnoozeUntil());
    msg.channel.sendDirect(reply, msg.member);
  }

  public String documentation() {
    return "/snooze (20s|45m|1h|2d) - snooze for a specified amount of time in seconds, minutes, hours, or days.";
  }

}
