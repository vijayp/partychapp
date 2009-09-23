package com.imjasonh.partychapp.server.command;
import java.util.Comparator;

import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.server.SendUtil;

public class StatusHandler extends SlashCommand {
  
  public StatusHandler() {
    super("status");
  }
        
  public void doCommand(Message msg, String argument) {
    String reply = "You are currently in '" + msg.channel.getName() + "' as '" + msg.member.getAlias() + "'";
    SendUtil.sendDirect(reply, msg.userJID, msg.serverJID);
  }
  
  public String documentation() {
    return "/status - show what room you're in";
  }

  public static class SortMembersForListComparator implements Comparator<Member> {
    public int compare(Member first, Member second) {
      // TODO: sort by online/offline, snoozing
      return first.getAlias().compareTo(second.getAlias());
    }
  }
}
