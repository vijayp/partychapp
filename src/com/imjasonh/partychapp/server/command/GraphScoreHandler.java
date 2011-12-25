package com.imjasonh.partychapp.server.command;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.ppb.Graphs;
import com.imjasonh.partychapp.ppb.Target;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

public class GraphScoreHandler extends SlashCommand {
  public GraphScoreHandler() {
    super("graph-score", "graph-scores");
  }

  @Override
  public void doCommand(Message msg, String argument, HttpServletResponse resp) {
    if (Strings.isNullOrEmpty(argument)) {
      msg.channel.sendDirect(
          "You must provide at least one target to graph", msg.member, resp);
      return;
    }
    
    String[] targetNames = argument.split(" ");
    List<Target> targets = Lists.newArrayList();
    for (String targetName : targetNames) {
      Target target = Datastore.instance().getTarget(msg.channel, targetName);
      if (target == null) {
        msg.channel.sendDirect("no target '" + targetName + "'", msg.member, resp);
        return;
      }
      targets.add(target);
    }

    msg.channel.sendDirect(
        Graphs.getScoreGraph(targets, 600, 500),
        msg.member, resp);
  }

  public String documentation() {
    return "/graph-score <target1> <target2> ... - prints a link to a graph of the changes to up to three targets' scores."; 
  }
}
