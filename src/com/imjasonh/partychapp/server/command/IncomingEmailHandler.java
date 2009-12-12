package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.Message.MessageType;

public class IncomingEmailHandler implements CommandHandler {
  public void doCommand(Message msg) {
    String reply = "**via email** " + msg.member.getAliasPrefix() + msg.content;
    msg.channel.broadcastIncludingSender(reply);
  }

  public String documentation() {
    // return null so this doesn't show up in /help documentation.
    return null;
  }

  public boolean matches(Message msg) {
    return msg.messageType.equals(MessageType.EMAIL);
  }
}
