package com.imjasonh.partychapp.server.command;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.Message;

public class SendBroadcastSMSHandler extends SlashCommand {

  public SendBroadcastSMSHandler() {
    super("broadcast-sms");
  }
  
  @Override
  void doCommand(Message msg, String argument, HttpServletResponse resp) {
    List<Member> recipients = msg.channel.broadcastSMS(msg.member.getAliasPrefix() + argument);
    String memberNames = "";
    for (Member m : recipients) {
      if (!memberNames.isEmpty()) {
        memberNames += ", ";
      }
      memberNames += m.getAlias();
    }

    msg.channel.broadcastIncludingSender("[" + msg.member.getAlias() + " **broadcasting to sms**] " +
                                         argument, resp);
    msg.channel.sendDirect("message was sent to: " + memberNames, msg.member, resp);
  }

  public String documentation() {
    return null;
  }

}
