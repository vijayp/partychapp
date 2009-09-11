package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.server.SendUtil;

/**
 * Action taken when a user messages a channel that does not exist yet. Channel is created and user
 * automatically joins the channel
 * 
 * @author imjasonh@gmail.com
 */
public class CreateAndJoinCommand implements CommandHandler {

  public void doCommand(Message msg) {
    assert msg.channel == null;
    assert msg.member == null;

    msg.channel = new Channel(msg.serverJID);
    msg.member = new Member(msg.userJID);
    msg.channel.addMember(msg.member);
    msg.channel.put();
    String reply = "The channel '" + msg.channel.getName() + "' has been created, " +
        "and you have joined with the alias '" + msg.member.getAlias() + "'";
    SendUtil.sendDirect(reply, msg.userJID, msg.serverJID);
  }

  public String documentation() {
    return null;
  }

  public boolean matches(Message msg) {
    return false;
  }

}
