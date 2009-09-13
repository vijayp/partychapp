package com.imjasonh.partychapp.server.command;

import java.util.List;

import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.ppb.PlusPlusBot;
import com.imjasonh.partychapp.ppb.Reason;
import com.imjasonh.partychapp.ppb.PlusPlusBot.Action;
import com.imjasonh.partychapp.server.SendUtil;

public class PPBHandler implements CommandHandler {
  PlusPlusBot ppb = new PlusPlusBot();
  
  public void doCommand(Message msg) {
    List<Reason> reasons = ppb.extractReasons(msg);
    StringBuilder sb = new StringBuilder();
    sb.append(msg.member.getAliasPrefix());
    if (reasons.isEmpty()) {
      sb.append(msg.content);
      SendUtil.broadcast(sb.toString(), msg.channel, msg.userJID, msg.serverJID);     
      return;
    };
    int pos = 0;
    // for "whee x++ and y-- yay" we want to change it into
    // "whee x++ [woot! now at 1] and y-- [ouch! now at -1] yay"
    for (Reason r : reasons) {
      // look for x++;
      String toSearch = r.target().name();
      toSearch += (r.action() == Action.PLUSPLUS) ? "++" : "--";
      int nextPos = msg.content.indexOf(toSearch, pos) + toSearch.length() + 1;
      boolean addSpaceAtEnd = true;
      // append "whee x++ "
      if (nextPos >= msg.content.length()) {
        sb.append(msg.content.substring(pos, msg.content.length()));
        sb.append(" ");
        nextPos = msg.content.length();
        addSpaceAtEnd = false;
      } else {
        sb.append(msg.content.substring(pos, nextPos));
      }
      // move the cursor to the "a" in "and"
      pos = nextPos;
      // append "[woot! now at 1] "
      sb.append(r.action() == PlusPlusBot.Action.PLUSPLUS ? "[woot! " : "[ouch! ");
      sb.append("now at " + r.scoreAfter() + "]");
      if (addSpaceAtEnd) {
        sb.append(" ");
      }
    }
    sb.append(msg.content.substring(pos));
    SendUtil.broadcastIncludingSender(sb.toString(), msg.channel, msg.userJID, msg.serverJID);
  }

  public boolean matches(Message msg) {
    return ppb.matches(msg.content);
  }

  public String documentation() {
    return "plusplusbot: handles ++'s and --'s";
  }
}