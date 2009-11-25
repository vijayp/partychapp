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
    String[] arguments = argument.split(" ", 2);
    
    if (arguments.length == 0) {
    	return;
    }
    
    String userArg = arguments[0].trim();
    StringBuilder didYouMean = new StringBuilder();
    Member toSummon = msg.channel.getOrSuggestMemberFromUserInput(userArg, didYouMean);
    if (toSummon == null && didYouMean.length() != 0) {
      msg.channel.broadcastIncludingSender(didYouMean.toString());
      return;
    }
    String emailBody = String.format("%s has summoned you to '%s'.",
    		msg.member.getAlias(), msg.channel.getName());
    if (arguments.length == 2) {
    	emailBody += String.format("\n %s said: %s",msg.member.getAlias(),arguments[1]);
    }

    String reply = String.format("_%s summoned %s_",
    		msg.member.getAlias(), toSummon.getAlias());
    String emailTitle = String.format("You have been summoned to '%s'",
    		msg.channel.getName());

    String error = msg.channel.sendMail(emailTitle,
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
