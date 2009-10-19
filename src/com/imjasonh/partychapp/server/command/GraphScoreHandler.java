package com.imjasonh.partychapp.server.command;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;

import com.google.appengine.repackaged.com.google.common.collect.Lists;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.ppb.Reason;
import com.imjasonh.partychapp.ppb.Target;

public class GraphScoreHandler extends SlashCommand {
  public GraphScoreHandler() {
    super("graph-score", "graph-scores");
  }
  
  public String roundToThreePlaces(Double d) {
    String s = d.toString();
    return s.substring(0, Math.min(s.indexOf(".") + 4, s.length()));
  }

  public void doCommand(Message msg, String argument) {
    int lo = 0;
    int hi = 0;
    String y = "";
    String encodedTargets = "";
    String legend = "";

    String[] targets = argument.split(" ");
    for (String t : targets) {
      Target target = Datastore.instance().getTarget(msg.channel, t);
      if (target == null) {
        msg.channel.sendDirect("no target '" + argument + "'", msg.member);
        return;
      }
      
      try {
        if (!encodedTargets.isEmpty()) {
          encodedTargets += ",";
          legend += "|";
        }
        encodedTargets += URLEncoder.encode(target.name(), "UTF-8");
        legend += URLEncoder.encode(target.name(), "UTF-8");
      } catch (UnsupportedEncodingException e) {}
      
      List<Reason> reasons = Lists.newArrayList(Datastore.instance().getReasons(target, 0));
      // make it oldest-to-newest order
      Collections.reverse(reasons);
  
      // start t=0,score=0
      if (!y.isEmpty()) {
        y += "|";
      }
      y += "0";
      for (Reason r : reasons) {
        lo = Math.min(lo, r.scoreAfter());
        hi = Math.max(hi, r.scoreAfter());

        y += "," + r.scoreAfter();
      }
    }
    
    Double zeroBar = ((double)(0-lo)/(hi-lo));
    
    String url = "http://chart.apis.google.com/chart?chs=600x500&cht=lc";
    url += "&chl=" + encodedTargets;
    url += "&chd=t:" + y;
    url += "&chds=" + lo + "," + hi;
    url += "&chtt=score+graph+for+" + encodedTargets;
    url += "&chxt=y&chxr=0," + lo + "," + hi;
    url += "&chco=FF0000,0000FF,00FF00";
    url += "&chdl=" + legend;
    url += "&chm=r,000000,0," + roundToThreePlaces(zeroBar) + "," + roundToThreePlaces(zeroBar + .001); 

    msg.channel.sendDirect(url, msg.member);
  }

  public String documentation() {
    return "/graph-score <target1> <target2> ... - prints a link to a graph of the changes to up to three targets' scores."; 
  }
}
