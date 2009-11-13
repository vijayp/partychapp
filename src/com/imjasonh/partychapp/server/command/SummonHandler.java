package com.imjasonh.partychapp.server.command;

import java.util.logging.Logger;

import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.Message;

public class SummonHandler extends SlashCommand {
  @SuppressWarnings("unused")
  private static final Logger LOG = Logger.getLogger(SummonHandler.class.getName());
  
  private BroadcastHandler bcast = new BroadcastHandler();
  
  public SummonHandler() {
    super("summon");
  }
  
  @Override
  public void doCommand(Message msg, String argument) {
    bcast.doCommand(msg);
    
    Member toSummon = msg.channel.getMemberByAlias(argument.trim());
    if (toSummon == null) {
      String didYouMean = null;
      for (Member m : msg.channel.getMembers()) {
        if (m.getAlias().contains(argument.trim()) ||
            m.getJID().contains(argument.trim())) {
          didYouMean = m.getAlias();
          break;
        }
      }
      String reply = "Could not find member with alias '" + argument.trim() + ".'";
      if (didYouMean != null) {
        reply = reply + " Maybe you meant to /summon " + didYouMean + ".";
      }
      msg.channel.broadcastIncludingSender(reply);
      return;
    }
    String emailBody = msg.member.getAlias() + " has summoned you to '" + msg.channel.getName() + "'.";
    String reply = "_" + msg.member.getAlias() + " summoned " + toSummon.getAlias() + "_";

    String error = msg.channel.sendMail("You have been summoned to '" + msg.channel.getName() + "'",
                      emailBody,
                      toSummon.getEmail());
    if (error != null) {
      reply = error;
    }
    msg.channel.broadcastIncludingSender(reply);

    if (toSummon.getSnoozeUntil() != null) {
      toSummon.setSnoozeUntil(null);
      toSummon.put();
      msg.channel.broadcastIncludingSender("_" + toSummon.getAlias() + " is no longer snoozing_");
    }
  }

  public String documentation() {
    return "/summon <alias> - summons a person in the room by sending them an email.";
  }

}
