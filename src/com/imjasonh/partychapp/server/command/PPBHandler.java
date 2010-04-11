package com.imjasonh.partychapp.server.command;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.util.List;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.ppb.PlusPlusBot;
import com.imjasonh.partychapp.ppb.Reason;

public class PPBHandler implements CommandHandler {
  PlusPlusBot ppb = new PlusPlusBot();

  private void doCommandWithCustomizedReply(
      Message msg, String prefix, String suffix) {
    msg.member.addToLastMessages(msg.content);
    msg.channel.put();
    List<Reason> reasons = ppb.extractReasons(msg);

    // for "whee x++ and y-- yay" we want to change it into
    // "whee x++ [woot! now at 1] and y-- [ouch! now at -1] yay"
    List<String> strList =
        Lists.newArrayListWithCapacity(reasons.size() * 3 + 3);
    strList.add(prefix);
    strList.add(msg.content);
    int nextStartPos = 0;
    final String lcaseContent = msg.content.toLowerCase();
    for (Reason reason: reasons) {
      String searchString =
          reason.target().name() + reason.action().toString();
      int foundPos = lcaseContent.indexOf(searchString, nextStartPos);
      
      // zap the old "rest of the string" since we're going to do some cutting
      strList.remove(strList.size() - 1);
      
      // add the part between the last reason and this one:
      strList.add(msg.content.substring(nextStartPos, foundPos));
      
      // add the "x++" part
      nextStartPos = foundPos + searchString.length();
      strList.add(msg.content.substring(foundPos, nextStartPos));

      // add the "[woot x->1]" part
      strList.add(" ");
      strList.add(reason.wootString());
      
      // add the rest of the string
      strList.add(msg.content.substring(nextStartPos));      
    }
    strList.add(suffix);
    
    String outString = Joiner.on("").join(strList);

    if (reasons.isEmpty()) {
      msg.channel.broadcast(outString, msg.member);    
    } else {
      msg.channel.broadcastIncludingSender(outString);
    }
  }
  
  public void doCommandAsCorrection(Message msg) {
    doCommandWithCustomizedReply(msg, "_" + msg.member.getAlias() + 
                                 " meant ", "_");
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
      summary.append(undo.action().opposite().toString());
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

