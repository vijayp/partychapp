package com.imjasonh.partychapp.server.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.ppb.PlusPlusBot;
import com.imjasonh.partychapp.ppb.Target;
import com.imjasonh.partychapp.server.SendUtil;

public class ScoreHandler implements CommandHandler {
  private static Pattern pattern =
    Pattern.compile("/score\\s+(" + PlusPlusBot.targetPattern + ")");
  
  @Override
  public void doCommand(Message msg) {
  String content = msg.content.trim();
    Matcher m = pattern.matcher(content);
    m.find();
    String name = m.group(1);
    Target target = Datastore.instance().getTarget(msg.channel, name);
    String reply;
    if (target == null) {
      reply = "no scores found";
    } else {
      reply = name + ": " + target.score();
    }
    SendUtil.sendDirect(reply, msg.userJID, msg.serverJID);
  }

  @Override
  public String documentation() {
    return "/score - see scores in plusplusbot";
  }

  @Override
  public boolean matches(Message msg) {
    return pattern.matcher(msg.content.trim()).matches();
  }
}
