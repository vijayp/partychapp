package com.imjasonh.partychapp.server.command;

import com.google.appengine.api.xmpp.JID;
import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.Member.SnoozeStatus;
import com.imjasonh.partychapp.server.SendUtil;

public class ListHandler implements CommandHandler {

  @Override
  public void doCommand(String content, JID userJID, JID serverJID, Member member, Channel channel) {
    StringBuilder sb = new StringBuilder()
        .append("Listing members of '")
        .append(channel.getName())
        .append("'");
    for (Member m : channel.getMembers()) {
      sb.append('\n')
          .append("* ")
          .append(m.getAlias())
          .append(" (")
          .append(m.getJID())
          .append(")");
      if (m.getSnoozeStatus() == SnoozeStatus.SNOOZING) {
        sb.append(" _snoozing_");
      }
    }

    SendUtil.sendDirect(sb.toString(), userJID, serverJID);
  }
}
