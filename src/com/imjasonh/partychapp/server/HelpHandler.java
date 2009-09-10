package com.imjasonh.partychapp.server;

import com.google.appengine.api.xmpp.JID;
import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Member;

public class HelpHandler extends CommandHandler {

  @Override
  void doCommand(String content, JID userJID, JID serverJID, Member member, Channel channel) {
    StringBuilder sb = new StringBuilder().append("List of commands:").append('\n');
    for (Command command : Command.values()) {
      sb.append("* ")
          .append(command.getDocumentation())
          .append('\n');
    }
    sb.append("* Message echo@ to hear yourself talk").append('\n');

    sendDirect(sb.toString(), userJID, serverJID);
  }

}
