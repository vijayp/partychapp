package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.server.SendUtil;

/**
 * Action taken when the user is not yet a member of the *existing* room.
 * 
 * @author imjasonh@gmail.com
 */
public class JoinCommand implements CommandHandler {

  public void doCommand(Message msg) {
    assert msg.channel != null;
    assert msg.member == null;

    msg.member = new Member(msg.userJID);
    msg.channel.addMember(msg.member);
    msg.channel.put();

    String reply = "You have joined '" + msg.channel.getName() + "' with the alias '"
        + msg.member.getAlias() + "'";
    SendUtil.sendDirect(reply, msg.userJID, msg.serverJID);

    String broadcast = msg.member.getJID() + "has joined the channel with the alias '"
        + msg.member.getAlias() + "'";
    SendUtil.broadcast(broadcast, msg.channel, msg.userJID, msg.serverJID);
  }

  public String documentation() {
    return null;
  }

  public boolean matches(Message msg) {
    return false;
  }

}
