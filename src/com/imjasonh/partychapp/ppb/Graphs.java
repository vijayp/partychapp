package com.imjasonh.partychapp.ppb;

import com.google.common.collect.Lists;

import com.imjasonh.partychapp.Datastore;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;

public class Graphs {
  private static String roundToThreePlaces(Double d) {
    String s = d.toString();
    return s.substring(0, Math.min(s.indexOf(".") + 4, s.length()));
  }
  
  public static String getScoreGraph(
      List<Target> targets, int width, int height) {
    int lo = 0;
    int hi = 0;
    StringBuffer y = new StringBuffer();
    StringBuffer encodedTargets = new StringBuffer();
    StringBuffer legend = new StringBuffer();
    
    for (Target target : targets) {
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
    
    StringBuffer url = new StringBuffer("http://chart.apis.google.com/chart?cht=lc");
    // dimensions
    url.append("&chs=").append(width).append("x").append(height);
    // labels for lines
    if (targets.size() > 1) {
      url.append("&chl=" + encodedTargets);
    }
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
    if (targets.size() > 1) {
      url.append("&chdl=" + legend);
    }
    // line at y=0
    Double zeroBar = ((double)(0-lo)/(hi-lo));
    url.append("&chm=r,000000,0," + roundToThreePlaces(zeroBar) + "," + roundToThreePlaces(zeroBar + .001));   
    
    return url.toString();
  }
}
