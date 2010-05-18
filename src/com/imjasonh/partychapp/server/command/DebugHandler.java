package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.DebuggingOptions.Option;
import com.imjasonh.partychapp.Message;


public class DebugHandler extends SlashCommand {
  public DebugHandler() {
    super("debug");
  }

  @Override
  void doCommand(Message msg, String argument) {
    String reply;
    if (argument == null) {
      argument = "";
    }
    
    Option option = Option.fromString(argument);
    
    if (option != null) {
      msg.member.debugOptions().add(option);
      msg.channel.put();
      reply = "enabling " + argument + " for you";
    } else if (argument.equals("clear")) {
      msg.member.debugOptions().clear();
      msg.channel.put();
      reply = "clearing all debug options";
    } else {
      reply = "Your current debug options are: " + msg.member.debugOptions();
    }
    msg.channel.sendDirect(reply, msg.member);
  }

  public String documentation() {
    return "/debug [sequenceIds|errorNotifications|clear] - " +
        "if you don't know what this does, you probably shouldn't use it";
  }

}
