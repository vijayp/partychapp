package com.imjasonh.partychapp.server.command;

import com.google.common.base.Strings;

import java.text.DateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import com.imjasonh.partychapp.Message;

public class SnoozeHandler extends SlashCommand {
  private static final String DETAILED_USAGE = 
      "You must specify a number and a unit of time to snooze " +
      "for -- for example, 20s for 20 seconds, 45m for 45 minutes, 1h for 1 " +
      "hour, or 2d for 2 days.";  
  
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
  void doCommand(Message msg, String argument, HttpServletResponse resp) {
    if (Strings.isNullOrEmpty(argument)) {
      msg.channel.sendDirect(
          "No snooze time period given. " + DETAILED_USAGE, msg.member, resp);
      return;       
    }
    
    argument = argument.trim();
    char unit = argument.charAt(argument.length() - 1);
    int num = 0;
    try {
      num = Integer.valueOf(argument.substring(0, argument.length() - 1));
    } catch (NumberFormatException e) {
      msg.channel.sendDirect(
          "Sorry, couldn't understand the time period you asked for. " + 
              DETAILED_USAGE,
          msg.member, resp);
      return;
    }
    if (num < 0) {
      msg.channel.sendDirect(
          "You can't snooze for a negative number of seconds! " + DETAILED_USAGE,
          msg.member, resp);
      return;
    }

    String unitToPrint = "";
    long seconds = 0;
    switch (unit) {
    case 's':
      unitToPrint = "seconds";
      seconds = num;
      break;
    case 'm':
      unitToPrint = "minutes";
      seconds = num * 60L;
      break;
    case 'h':
      unitToPrint = "hours";
      seconds = num * 60L * 60L;
      break;
    case 'd':
      unitToPrint = "days";
      seconds = num * 60L * 60L * 24L;
      break;
    default:
      msg.channel.sendDirect(DETAILED_USAGE, msg.member, resp);
      return;
    }

    msg.member.setSnoozeUntil(new Date(now() + seconds * 1000L));
    msg.channel.put();
    String reply = "Okay, snoozing for " + num + " " + unitToPrint +
        " (" + seconds + " seconds), until " +
        df.format(msg.member.getSnoozeUntil());
    msg.channel.sendDirect(reply, msg.member, resp);
  }

  public String documentation() {
    return "/snooze (20s|45m|1h|2d) - snooze for a specified amount of time in seconds, minutes, hours, or days.";
  }

}
