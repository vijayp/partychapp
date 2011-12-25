package com.imjasonh.partychapp.server.command;

import javax.servlet.http.HttpServletResponse;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.ppb.PlusPlusBot;
import com.imjasonh.partychapp.ppb.Target;

public class ScoreHandler extends SlashCommand {
  
  ScoreHandler() {
    super("score\\s+(" + PlusPlusBot.targetPattern + ")");
  }

  @Override
  public void doCommand(Message msg, String name, HttpServletResponse resp) {
    // TODO: Validate target pattern
    Target target = Datastore.instance().getTarget(msg.channel, name);
    String reply;
    if (target == null) {
      reply = "no scores found";
    } else {
      reply = name + ": " + target.score();
    }
    msg.channel.sendDirect(reply, msg.member, resp);
  }

  public String documentation() {
    return "/score target - see the score of 'target' in plusplusbot";
  }
}
