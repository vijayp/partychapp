package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;

public class BroadcastHandler implements CommandHandler {

  public void doCommand(Message msg) {
    msg.member.addToLastMessages(msg.content);
    String reply = msg.member.getAliasPrefix() + msg.content;
    msg.channel.broadcast(reply, msg.member);
  }

  public String documentation() {
    // return null so this doesn't show up in /help documentation.
    return null;
  }

  public boolean matches(Message msg) {
    // This has to be last in the list, because it swallows everything.
    return true;
  }

}
