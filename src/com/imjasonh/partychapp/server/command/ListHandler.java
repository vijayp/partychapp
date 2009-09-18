package com.imjasonh.partychapp.server.command;

import com.google.appengine.api.xmpp.JID;
import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.Member.SnoozeStatus;
import com.imjasonh.partychapp.server.SendUtil;

public class ListHandler extends SlashCommand {
  
	public ListHandler() {
    super("list", "names");
  }
	
  public void doCommand(Message msg, String argument) {
    // TODO: Reject or act on non-null argument
    
    // TODO: sort by online/offline, snoozing, alias
    StringBuilder sb = new StringBuilder()
        .append("Listing members of '")
        .append(msg.channel.getName())
        .append("'");
    for (Member m : msg.channel.getMembers()) {
      sb.append('\n')
          .append("* ")
          .append(m.getAlias())
          .append(" (")
          .append(m.getJID())
          .append(")");
      if (SendUtil.getPresence(new JID(m.getJID()))) {
        sb.append(" (online)");
      }
      if (m.getSnoozeStatus() == SnoozeStatus.SNOOZING) {
        sb.append(" _snoozing_");
      }
    }

    SendUtil.sendDirect(sb.toString(), msg.userJID, msg.serverJID);
  }
  
  public String documentation() {
	  return "/list - show members of room";
  }
}
