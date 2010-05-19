package com.imjasonh.partychapp.server.command;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import com.imjasonh.partychapp.Configuration;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.server.InviteUtil;

import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
 * Action taken when the user invites other to the current channel.
 * 
 * @author kushaldave@gmail.com
 */
public class InviteHandler extends SlashCommand {

  public InviteHandler() {
    super("invite");
  }

  @Override
  public void doCommand(Message msg, String jids) {
    assert msg.channel != null;
    assert msg.member != null;
    
    if (Strings.isNullOrEmpty(jids)) {
      msg.channel.sendDirect(
          "Please list some email addresses to invite", msg.member);
      return;
    }
    
    List<String> jidsToInvite = Lists.newArrayList();
    String error = parseEmailAddresses(jids, jidsToInvite);
    if (!error.isEmpty()) {
      msg.channel.sendDirect(error, msg.member);
    }
    
    for (String jidToInvite : jidsToInvite) {
      // Add undo support?
      // msg.member.addToLastMessages(msg.content);
      String inviteError = InviteUtil.invite(
          jidToInvite,
          msg.channel,
          msg.member.getAlias(),
          msg.member.getJID());
      msg.channel.invite(jidToInvite);
      msg.channel.put();
  
      String broadcast = "_" + msg.member.getAlias() + " invited " +
          jidToInvite + "_";

      if (!Strings.isNullOrEmpty(inviteError)) {
        broadcast += inviteError;
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
      invitee = invitee.trim();
      InternetAddress address = null;
      try {
        address = new InternetAddress(invitee);
        address.validate();
        if (!address.toString().contains("@")) {
          error.append(
              "Could not invite " + invitee + ". Did you mean " + invitee +
              "@gmail.com?\n");
          continue;
        }
        if (address.toString().endsWith(Configuration.chatDomain) ||
            address.toString().endsWith(Configuration.mailDomain)) {
          error.append(
              "Could not invite " + invitee + " (cannot invite other rooms)\n");
          continue;
        }
      } catch (AddressException e) {
        error.append(
            "Could not invite " + invitee + ". Is it a valid email address?\n");
        continue;
      }
      output.add(address.getAddress());
    }
    return error.toString();
  }
}
