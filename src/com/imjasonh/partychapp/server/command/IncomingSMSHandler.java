package com.imjasonh.partychapp.server.command;

import javax.servlet.http.HttpServletResponse;

import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.Message.MessageType;

public class IncomingSMSHandler implements CommandHandler {
  public void doCommand(Message msg) {
    doCommand(msg, null);
  }
  public void doCommand(Message msg, HttpServletResponse resp) {

    String reply = "*via SMS* ";
    reply += msg.member.getAliasPrefix();
    reply += msg.content;
    msg.member.addToLastMessages(msg.content);
    
    msg.channel.broadcastSMS(reply);

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
