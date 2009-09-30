package com.imjasonh.partychapp.server.command;

import java.util.List;

import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.ppb.PlusPlusBot;
import com.imjasonh.partychapp.ppb.Reason;

public class PPBHandler implements CommandHandler {
  PlusPlusBot ppb = new PlusPlusBot();

  private void doCommandWithCustomizedReply(Message msg, String prefix, String suffix) {
    StringBuilder sb = new StringBuilder(prefix);
    msg.member.addToLastMessages(msg.content);
    msg.channel.put();
    List<Reason> reasons = ppb.extractReasons(msg);
    if (reasons.isEmpty()) {
      sb.append(msg.content);
      sb.append(suffix);
      msg.channel.broadcast(sb.toString(), msg.member);
      return;
    }
    int pos = 0;
    // for "whee x++ and y-- yay" we want to change it into
    // "whee x++ [woot! now at 1] and y-- [ouch! now at -1] yay"
    for (Reason r : reasons) {
      // look for x++;
      String toSearch = r.target().name();
      toSearch += r.action().isPlusPlus() ? "++" : "--";
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
      sb.append(r.wootString());
      if (addSpaceAtEnd) {
        sb.append(" ");
      }
    }
    sb.append(msg.content.substring(pos));
    sb.append(suffix);
    msg.channel.broadcastIncludingSender(sb.toString());    
  }
  
  public void doCommandAsCorrection(Message msg) {
    doCommandWithCustomizedReply(msg, "_" + msg.member.getAlias() + " meant ", "_");
  }
  
  public void doCommand(Message msg) {
    doCommandWithCustomizedReply(msg, msg.member.getAliasPrefix(), "");
  }
  
  public void undoEarlierMessage(Message msg) {
    List<Reason> allUndos = ppb.undoEarlierMessage(msg);

    StringBuilder summary = new StringBuilder();
    boolean first = true;
    for (Reason undo : allUndos) {
      if (!first) {
        summary.append(", ");
      }
      summary.append(undo.target().name());
      summary.append(undo.action().opposite().isPlusPlus() ? "++" : "--");
      summary.append(" [back to ");
      summary.append(undo.scoreAfter());
      summary.append("]");
      first = false;
    }
    String str = summary.toString();
    if (!str.isEmpty()) {
      msg.channel.broadcastIncludingSender(("Undoing original actions: " + str));
    }
  }

  public boolean matches(Message msg) {
    return ppb.matches(msg.content);
  }

  public String documentation() {
    return "plusplusbot: handles ++'s and --'s";
  }
}
