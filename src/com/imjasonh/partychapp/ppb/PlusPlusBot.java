package com.imjasonh.partychapp.ppb;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.appengine.repackaged.com.google.common.base.Pair;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Message;

public class PlusPlusBot {
  private static Set<String> blacklist = new HashSet<String>();

  enum Action {
    PLUSPLUS, MINUSMINUS
  };

  public static Pattern pattern = Pattern.compile("(^|\\s)(\\w+)(\\+\\+|--)");

  static {
    blacklist.add("c");
  }

  public List<Reason> handleMessage(Message msg) {
    List<Reason> reasons = new ArrayList<Reason>();
    Set<Pair<Target, Action> > changed = new HashSet<Pair<Target, Action> >();
    boolean hasOverride = msg.content.contains("/combine");
    Matcher m = pattern.matcher(msg.content);
    while (m.find()) {
      // spaces are group 1, target is group 2, ++ or -- is group 3
      String target = m.group(2).toLowerCase();
      String action = m.group(3);
      if (blacklist.contains(target)) {
        continue;
      }
      Target t = Datastore.get().getOrCreateTarget(msg.channel, target);
      Action a = action.equals("--") ? Action.MINUSMINUS : Action.PLUSPLUS;
      if (!hasOverride) {
       if (changed.contains(new Pair<Target, Action>(t, a))) {
        continue;
       } else {
         changed.add(new Pair<Target, Action>(t, a));
       }
      }
      Reason r = t.takeAction(a, msg.content);
      reasons.add(r);
    }
    return reasons;
  };
}