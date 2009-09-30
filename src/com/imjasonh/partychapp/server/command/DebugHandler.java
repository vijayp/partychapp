package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.server.SendUtil;

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
    if (argument.equals("sequenceIds")) {
      msg.member.debugOptions().add("sequenceIds");
      msg.channel.put();
      reply = "enabling sequenceIds for you";
    } else if (argument.equals("clear")) {
      msg.member.debugOptions().clear();
      msg.channel.put();
      reply = "clearing all debug options";
    } else {
      reply = "Your current debug options are: " + msg.member.debugOptions();
    }
    SendUtil.sendDirect(reply, msg.userJID, msg.serverJID);
  }

  public String documentation() {
    return "/debug [sequenceIds|clear] - if you don't know what this does, you probably shouldn't use it";
  }

}
