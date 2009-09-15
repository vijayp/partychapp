package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.ppb.PlusPlusBot;
import com.imjasonh.partychapp.ppb.Target;
import com.imjasonh.partychapp.server.SendUtil;

public class ScoreHandler extends SlashCommand {
  
  ScoreHandler() {
    super("score\\s+(" + PlusPlusBot.targetPattern + ")");
  }

  public void doCommand(Message msg, String name) {
    // TODO: Validate target pattern
    Target target = Datastore.instance().getTarget(msg.channel, name);
    String reply;
    if (target == null) {
      reply = "no scores found";
    } else {
      reply = name + ": " + target.score();
    }
    SendUtil.sendDirect(reply, msg.userJID, msg.serverJID);
  }

  public String documentation() {
    return "/score - see scores in plusplusbot";
  }
}
