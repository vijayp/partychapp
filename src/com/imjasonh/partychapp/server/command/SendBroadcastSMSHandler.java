package com.imjasonh.partychapp.server.command;

import java.util.List;

import com.google.appengine.repackaged.com.google.common.collect.Lists;
import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.Message;

public class SendBroadcastSMSHandler extends SlashCommand {

  public SendBroadcastSMSHandler() {
    super("broadcast-sms");
  }
  
  @Override
  void doCommand(Message msg, String argument) {
    List<String> addresses = Lists.newArrayList();
    String memberNames = "";
    for (Member m : msg.channel.getMembers()) {
      if ((m.phoneNumber() != null) && 
          (m.carrier() != null)) {
        addresses.add(m.carrier().emailAddress(m.phoneNumber()));
        if (!memberNames.isEmpty()) {
          memberNames += ", ";
        }
        memberNames += m.getAlias();
      }
    }

    for (String addr : addresses) {
      msg.channel.sendMail("(sent from partychat)",
                           msg.member.getAliasPrefix() + argument,
                           addr);
    }
    msg.channel.broadcastIncludingSender("[" + msg.member.getAlias() + " **broadcasting to sms**] " +
                                         argument);
    msg.channel.sendDirect("message was sent to: " + memberNames, msg.member);
  }

  public String documentation() {
    return null;
  }

}
