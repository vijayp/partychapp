package com.imjasonh.partychapp.server.command;

import java.util.List;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.ppb.Reason;
import com.imjasonh.partychapp.ppb.Target;

public class ReasonsHandler extends SlashCommand {

  ReasonsHandler() {
    super("reasons");
  }

  @Override
  public void doCommand(Message msg, String name) {
    // TODO: Validate target pattern
    Target target = Datastore.instance().getTarget(msg.channel, name);
    StringBuilder sb = new StringBuilder();
    sb.append(name + ": " + target.score() + "\n");
    List<Reason> reasons = Datastore.instance().getReasons(target, 10);
    for (Reason r : reasons) {
      sb.append(r.action().ifPlusPlusElse("increment by ", "decrement by "));
      sb.append(r.sender().getJID());
      sb.append(" (" + r.reason() + ")\n");
    }
    msg.channel.sendDirect(sb.toString().trim(), msg.member);
  }

  public String documentation() {
    return "/reasons target - see why target's score was changed";
  }
}
