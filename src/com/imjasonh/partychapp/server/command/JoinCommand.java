package com.imjasonh.partychapp.server.command;

import java.util.logging.Logger;

import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.Message.MessageType;
import com.imjasonh.partychapp.server.SendUtil;

/**
 * Action taken when the user is not yet a member of the *existing* room.
 * 
 * @author imjasonh@gmail.com
 */
public class JoinCommand implements CommandHandler {

  @SuppressWarnings("unused")
  private static final Logger LOG = Logger.getLogger(JoinCommand.class.getName());
  
  public void doCommand(Message msg) {
    assert msg.channel != null;
    assert msg.member == null;

    String email = msg.userJID.getId().split("/")[0];
    if (!msg.channel.canJoin(email)) {
      String reply = "You must be invited to this room.";
      //TODO(vijayp): migrated bots are getting all confused and DOSing the service. 
      // Do not send them a message. 
      if (!msg.channel.isMigrated()) {
        SendUtil.sendDirect(reply, msg.userJID, msg.serverJID);
      }
      return;
    }

    msg.member = msg.channel.addMember(msg.user);
    msg.channel.put();

    String reply = "You have joined '" + msg.channel.getName() + "' with the alias '"
        + msg.member.getAlias() + "'";
    msg.channel.sendDirect(reply, msg.member);

    String broadcast = msg.member.getJID() + " has joined the channel with the alias '"
        + msg.member.getAlias() + "'";
    msg.channel.broadcast(broadcast, msg.member);
    
    Command.getCommandHandler(msg).doCommand(msg);
  }

  public String documentation() {
    return null;
  }

  public boolean matches(Message msg) {
    return msg.channel != null && msg.member == null && msg.messageType.equals(MessageType.XMPP);
  }

}
