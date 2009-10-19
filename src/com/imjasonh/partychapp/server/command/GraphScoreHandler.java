package com.imjasonh.partychapp.server.command;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.ppb.Reason;
import com.imjasonh.partychapp.ppb.Target;

public class GraphScoreHandler extends SlashCommand {
  private static final char[] encoding = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-.".toCharArray();
  
  public GraphScoreHandler() {
    super("graph-score", "graph-scores");
  }

  public void doCommand(Message msg, String argument) {
    Target target = Datastore.instance().getTarget(msg.channel, argument);
    if (target == null) {
      msg.channel.sendDirect("no target '" + argument + "'", msg.member);
      return;
    }
    List<Reason> reasons = Datastore.instance().getReasons(target, 0);
    // make it oldest-to-newest order
    Collections.reverse(reasons);

    int lo = 0;
    int hi = 0;
    String y = "";
    for (Reason r : reasons) {
      lo = Math.min(lo, r.scoreAfter());
      hi = Math.max(hi, r.scoreAfter());

      y += r.scoreAfter() + ",";
    }
    y = y.substring(0, y.length() - 1);

    String url = "http://chart.apis.google.com/chart?chs=600x500&cht=lc";
    try {
      url += "&chl=" + URLEncoder.encode(argument, "UTF-8");
    } catch (UnsupportedEncodingException e) {}
    url += "&chd=t:" + y;
    url += "&chds=" + lo + "," + hi;
    url += "&chxt=y&chxr=0," + lo + "," + hi;

    msg.channel.sendDirect(url, msg.member);    
  }

  public String documentation() {
    return "/graph-score <target1> <target2> ... - prints a link to a graph of the changes to one or more targets' scores."; 
  }
}
