package com.imjasonh.partychapp.server.command;

import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import com.google.appengine.repackaged.com.google.common.collect.Lists;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.server.SendUtil;

/**
 * Action taken when the user
 * 
 * @author kushaldave@gmail.com
 */
public class InviteHandler extends SlashCommand {

  public InviteHandler() {
    super("invite");
  }

  public void doCommand(Message msg, String jids) {
    assert msg.channel != null;
    assert msg.member != null;
    
    List<String> jidsToInvite = Lists.newArrayList();
    String error = parseEmailAddresses(jids, jidsToInvite);
    if (!error.isEmpty()) {
      msg.channel.sendDirect(error, msg.member);
    }
    
    for (String jidToInvite : jidsToInvite) {
      // Add undo support?
      // msg.member.addToLastMessages(msg.content);
      SendUtil.invite(jidToInvite, msg.serverJID);
      msg.channel.invite(jidToInvite);
      msg.channel.put();
  
      String broadcast = "_" + msg.member.getAlias() + " invited " +
          jidToInvite + "_";
  
      String subject = String.format("%s invited you to '%s'",
                                     msg.member.getAlias(),
                                     msg.channel.getName());
      String body =
        String.format("%s (%s) invited you to a chatroom named '%s'.\n\n" +
                      "To join, please accept the chat request from %s, " +
                      "and send it an IM. That will automatically enter you into the chatroom. Once you're in " +
                      "the room, try sending '/help' to it over IM for a handy list of what it can do.\n\n" +
                      "For information about how to invite someone to chat in gmail, read " +
                      "http://mail.google.com/support/bin/answer.py?answer=33508.\n" +
                      "For more information about partychat, try http://code.google.com/p/partychapp/.\n",
                      msg.member.getAlias(), msg.member.getJID(), msg.channel.getName(),
                      msg.serverJID.getId());
      String mailError = msg.channel.sendMail(subject, body, jidToInvite);
      if (mailError != null) {
        broadcast = mailError;
      }
      
      msg.channel.broadcastIncludingSender(broadcast);
    }
  }

  public String documentation() {
    return "/invite email1@foo.com, email2@bar.com ... - Invite a list of email addresses to this room";
  }

  public static String parseEmailAddresses(String invitees, List<String> output) {
    StringBuilder error = new StringBuilder();
    for (String invitee : invitees.split(",")) {
      InternetAddress address = null;
      try {
        address = new InternetAddress(invitee);
        address.validate();
      } catch (AddressException e) {
        error.append("Could not invite " + invitee + ". Is it a valid email address?\n");
        continue;
      }
      output.add(address.getAddress());
    }
    return error.toString();
  }
}
