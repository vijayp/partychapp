package com.imjasonh.partychapp.server.command;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import com.imjasonh.partychapp.Configuration;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.server.InviteUtil;

import java.util.List;
import java.util.regex.Pattern;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletResponse;

/**
 * Action taken when the user invites other to the current channel.
 * 
 * @author kushaldave@gmail.com
 */
public class InviteHandler extends SlashCommand {
  
  /**
   * Slightly expanded definition of valid domain name characters, based on
   * LDH pattern from RFC 1035.
   */
  private static final Pattern DOMAIN_PATTERN =
      Pattern.compile("^[A-Za-z0-9-_.]+$");

  public InviteHandler() {
    super("invite");
  }

  @Override
  public void doCommand(Message msg, String jids, HttpServletResponse resp) {
    assert msg.channel != null;
    assert msg.member != null;
    
    if (Strings.isNullOrEmpty(jids)) {
      msg.channel.sendDirect(
          "Please list some email addresses to invite", msg.member, resp);
      return;
    }
    
    List<String> jidsToInvite = Lists.newArrayList();
    String error = parseEmailAddresses(jids, jidsToInvite);
    if (!error.isEmpty()) {
      msg.channel.sendDirect(error, msg.member, resp);
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
      
      msg.channel.broadcastIncludingSender(broadcast, resp);
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
        if (!address.getAddress().contains("@")) {
          error.append(
              "Could not invite \"" + invitee + "\". Did you mean \"" +
              invitee + "@gmail.com\"?\n");
          continue;
        }
        String domain = address.getAddress().split("@")[1];
        if (!DOMAIN_PATTERN.matcher(domain).matches()) {
          error.append(
              "Could not invite \"" + invitee +
              "\". Is it a valid email address?\n");
          continue;
        }
        if (domain.endsWith(Configuration.chatDomain) ||
            domain.endsWith(Configuration.mailDomain)) {
          error.append(
              "Could not invite \"" + invitee +
              "\" (cannot invite other rooms)\n");
          continue;
        }
      } catch (AddressException e) {
        error.append(
            "Could not invite \"" + invitee +
            "\". Is it a valid email address?\n");
        continue;
      }
      output.add(address.getAddress());
    }
    return error.toString();
  }
}
