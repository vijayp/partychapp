package com.imjasonh.partychapp.server;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Configuration;

/**
 * Utility functions for sending invitations.
 * 
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class InviteUtil {
  public static String invite(
      String inviteeEmail,
      Channel channel,
      String inviterAlias,
      String inviterEmail) {
    // XMPP invitation
    SendUtil.invite(inviteeEmail, channel.serverJID());
    
    // Email invitation
    String subject = String.format("%s invited you to '%s'",
        inviterAlias, channel.getName());
    String inviterName;
    
    if (inviterAlias.equals(inviterEmail)) {
      inviterName = inviterAlias;
    } else {
      inviterName = String.format("%s (%s)", inviterAlias, inviterEmail);
    }
    String body = String.format(
        "%s invited you to a chatroom named '%s'.\n\n" +
        "To join, please accept the chat request from %s, and send it an IM. " +
        "That will automatically enter you into the chatroom. Once you're in " +
        "the room, try sending '/help' to it over IM for a handy list of " +
        "what it can do.\n\n" +
        "You can also accept the invitation (and see who else is in the " +
        "room) at: %s\n\n" +
        "For information about how to invite someone to chat in Gmail, read " +
        "http://mail.google.com/support/bin/answer.py?answer=33508.\n\n" +
        "For more information about Partychat, try http://%s\n",
        inviterName, 
        channel.getName(),
        channel.serverJID().getId(),
        channel.webUrl(),
        Configuration.webDomain);
    return channel.sendMail(subject, body, inviteeEmail);    
  }
  
  private InviteUtil() {
    // Not instantiable
  }
}
