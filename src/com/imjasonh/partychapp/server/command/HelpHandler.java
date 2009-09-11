package com.imjasonh.partychapp.server.command;

import com.google.appengine.api.xmpp.JID;
import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.server.SendUtil;

public class HelpHandler implements CommandHandler {

  public void doCommand(String content, JID userJID, JID serverJID, Member member, Channel channel) {
    StringBuilder sb = new StringBuilder().append("List of commands:").append('\n');
    for (Command command : Command.values()) {
      sb.append("* ")
          .append(command.getDocumentation())
          .append('\n');
    }
    sb.append("* Message echo@ to hear yourself talk").append('\n');

    SendUtil.sendDirect(sb.toString(), userJID, serverJID);
  }

}
