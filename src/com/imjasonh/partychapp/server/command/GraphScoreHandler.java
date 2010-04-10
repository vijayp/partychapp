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

  @Override
  public void doCommand(Message msg, String argument) {
    int lo = 0;
    int hi = 0;
    StringBuffer y = new StringBuffer();
    StringBuffer encodedTargets = new StringBuffer();
    StringBuffer legend = new StringBuffer();

    String[] targets = argument.split(" ");
    for (String t : targets) {
      Target target = Datastore.instance().getTarget(msg.channel, t);
      if (target == null) {
        msg.channel.sendDirect("no target '" + t + "'", msg.member);
        return;
      }
      
      String encoded = "";
      try {
        encoded = URLEncoder.encode(target.name(), "UTF-8");
      } catch (UnsupportedEncodingException e) {}

      if (encodedTargets.length() > 0) {
        encodedTargets.append(",");
        legend.append("|");
      }
      encodedTargets.append(encoded);
      legend.append(encoded);
      
      List<Reason> reasons = Lists.newArrayList(Datastore.instance().getReasons(target, 0));
      // make it oldest-to-newest order
      Collections.reverse(reasons);
  
      if (y.length() > 0) {
        y.append("|");
      }
      // start t=0,score=0
      y.append("0");
      for (Reason r : reasons) {
        lo = Math.min(lo, r.scoreAfter());
        hi = Math.max(hi, r.scoreAfter());

        y.append("," + r.scoreAfter());
      }
    }
    
    StringBuffer url = new StringBuffer("http://chart.apis.google.com/chart?chs=600x500&cht=lc");
    // labels for lines
    url.append("&chl=" + encodedTargets);
    // data
    url.append("&chd=t:" + y);
    // scaling factor
    url.append("&chds=" + lo + "," + hi);
    // title
    url.append("&chtt=score+graph+for+" + encodedTargets);
    // dimensions
    url.append("&chxt=y&chxr=0," + lo + "," + hi);
    // colors
    url.append("&chco=FF0000,0000FF,00FF00");
    // legend
    url.append("&chdl=" + legend);
    // line at y=0
    Double zeroBar = ((double)(0-lo)/(hi-lo));
    url.append("&chm=r,000000,0," + roundToThreePlaces(zeroBar) + "," + roundToThreePlaces(zeroBar + .001)); 

    msg.channel.sendDirect(url.toString(), msg.member);
  }

  public String documentation() {
    return "/graph-score <target1> <target2> ... - prints a link to a graph of the changes to up to three targets' scores."; 
  }
}
