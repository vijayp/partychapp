package com.imjasonh.partychapp.server.command;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.ppb.PlusPlusBot;
import com.imjasonh.partychapp.ppb.Reason;
import com.imjasonh.partychapp.ppb.Target;
import com.imjasonh.partychapp.server.SendUtil;

public class ReasonsHandler implements CommandHandler {
  private static Pattern pattern =
    Pattern.compile("/reasons\\s+(" + PlusPlusBot.targetPattern + ")");
  
  public void doCommand(Message msg) {
    Matcher m = pattern.matcher(msg.content.trim());
    m.find();
    String name = m.group(1);
    Target target = Datastore.instance().getTarget(msg.channel, name);
    StringBuilder sb = new StringBuilder();
    sb.append(name + ": " + target.score() + "\n");
    List<Reason> reasons = Datastore.instance().getReasons(target, 10);
    for (Reason r : reasons) {
      sb.append(r.action() == PlusPlusBot.Action.PLUSPLUS ? "increment by " : "decrement by ");
      sb.append(r.sender().getJID());
      sb.append(" (" + r.reason() + ")\n");
    }
    SendUtil.sendDirect(sb.toString().trim(), msg.userJID, msg.serverJID);
  }

  public String documentation() {
    return "/reasons - see why someone's score was changed";
  }

  public boolean matches(Message msg) {
    return pattern.matcher(msg.content.trim()).matches();
  }
}
