package com.imjasonh.partychapp.server.command;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.ppb.Reason;
import com.imjasonh.partychapp.ppb.Target;

public class ReasonsHandler extends SlashCommand {

  ReasonsHandler() {
    super("reasons");
  }

  @Override
  public void doCommand(Message msg, String name, HttpServletResponse resp) {
    // TODO: Validate target pattern
    Target target = Datastore.instance().getTarget(msg.channel, name);
    StringBuilder sb = new StringBuilder();
    
    if (target == null ) {
      sb.append("No reasons found");
    } else {
      sb.append(name + ": " + target.score() + "\n");
      List<Reason> reasons = Datastore.instance().getReasons(target, 10);
      for (Reason r : reasons) {
        sb.append(r.action().ifPlusPlusElse("increment by ", "decrement by "));
        sb.append(r.senderAlias());
        sb.append(" (" + r.reason() + ")\n");
      }
      
      if (reasons.size() == 10) {
        sb.append("More reasons may be visible at " + msg.channel.webUrl());
      }
    }
    msg.channel.sendDirect(sb.toString().trim(), msg.member, resp);    
  }

  public String documentation() {
    return "/reasons target - see why target's score was changed";
  }
}
