package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.server.SendUtil;

public class BroadcastHandler implements CommandHandler {

  public void doCommand(Message msg) {
    msg.member.addToLastMessages(msg.content);
    String reply = msg.member.getAliasPrefix() + msg.content;
    SendUtil.broadcast(reply, msg.channel, msg.serverJID, msg.userJID);
  }

  public String documentation() {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean matches(Message msg) {
    // This has to be last in the list, because it swallows everything.
    return true;
  }

}
