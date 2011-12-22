package com.imjasonh.partychapp.server.command;

import javax.servlet.http.HttpServletResponse;

import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.Message.MessageType;

public class BroadcastHandler implements CommandHandler {
  public void doCommand(Message msg) {
    doCommand(msg, null);
  }
  public void doCommand(Message msg, HttpServletResponse resp) {
    String reply = msg.member.getAliasPrefix() + msg.content;
    msg.channel.broadcast(reply, msg.member, resp);
    
    // we should only need to put the channel object if we saved the message. Otherwise
    // there's no need.
    if (msg.member.addToLastMessages(msg.content))  {
      msg.channel.put();
    } else {
      ;
    }
  }

  public String documentation() {
    // return null so this doesn't show up in /help documentation.
    return null;
  }

  public boolean matches(Message msg) {
    // This has to be last in the list, because it swallows every XMPP message.
    return msg.messageType.equals(MessageType.XMPP);
  }
}
