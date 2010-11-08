package com.imjasonh.partychapp.server.command;

import com.google.appengine.api.xmpp.JID;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.Member.SnoozeStatus;
import com.imjasonh.partychapp.server.SendUtil;

import java.util.Collections;
import java.util.List;

public class ListHandler extends SlashCommand {
  
  public ListHandler() {
    super("list", "names", "who", "members");
  }
	
  @Override
  public void doCommand(Message msg, String argument) {
    boolean isFiltering = !Strings.isNullOrEmpty(argument);
    
    List<Member> members = Lists.newArrayList(msg.channel.getMembers());
    List<String> invitees = Lists.newArrayList(msg.channel.getInvitees());
     
    if (isFiltering) {
      List<Member> filteredMembers = Lists.newArrayList();
      for (Member m : members) {
        if (m.getAlias().contains(argument) ||
            m.getJID().contains(argument)) {
          filteredMembers.add(m);
        }
      }
      members = filteredMembers;
      
      List<String> filteredInvitees = Lists.newArrayList();
      for (String invitee : invitees) {
        if (invitee.contains(argument)) {
          filteredInvitees.add(invitee);
        }
      }
      invitees = filteredInvitees;
    }
    
    Collections.sort(members, new Member.SortMembersForListComparator());
    StringBuilder sb = new StringBuilder()
        .append("Listing members of '")
        .append(msg.channel.getName())
        .append("'");
    if (isFiltering) {
      sb.append(" that match '" + argument + "'");
    }
    for (Member m : members) {
      sb.append('\n')
          .append("* ")
          .append(m.getAlias())
          .append(" (")
          .append(m.getJID())
          .append(")");
      if (SendUtil.getPresence(new JID(m.getJID()), msg.channel.serverJID())) {
        sb.append(" (online)");
      }
      if (m.getSnoozeStatus() == SnoozeStatus.SNOOZING) {
        sb.append(" _snoozing_");
      }
    }

    if (msg.channel.isInviteOnly() && !isFiltering) {
      sb.append("\nRoom is invite-only.");
    }
    for (String invitee : invitees) {
      sb.append("\nInvited: ").append(invitee);
    }
    msg.channel.sendDirect(sb.toString(), msg.member);
  }
  
  public String documentation() {
	  return "/list [filter] - show members of room, optionally filtered " +
	      "to only matching members";
  }
}
