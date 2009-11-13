package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.Message.MessageType;

public class SMSHandler implements CommandHandler {
  public void doCommand(Message msg) {
    String reply = "**via SMS (" + msg.phoneNumber + ")** ";
    if (msg.member != null) {
      reply += msg.member.getAliasPrefix();
      msg.member.addToLastMessages(msg.content);
    } else {
      reply += "[no member found] ";
    }
    reply += msg.content;

    msg.channel.broadcastIncludingSender(reply);
    msg.channel.put();
  }

  public String documentation() {
    // return null so this doesn't show up in /help documentation.
    return null;
  }

  public boolean matches(Message msg) {
    return msg.messageType.equals(MessageType.SMS);
  }
}
