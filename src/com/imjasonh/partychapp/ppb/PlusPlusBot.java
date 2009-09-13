package com.imjasonh.partychapp.ppb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Message;

public class PlusPlusBot {
  private static Set<String> blacklist = new HashSet<String>();

  public enum Action {
    PLUSPLUS, MINUSMINUS
  };

  public static final String targetPattern = "[\\w-\\.]+";
  private static final Pattern pattern =
    Pattern.compile("(" + targetPattern + ")(\\+\\+|--)");

  static {
    // c++
    blacklist.add("c");
  }

  public boolean matches(String content) {
    // use find() instead of matches() because matches() looks for the whole
    // string to match and we're okay with substring matches.
    return pattern.matcher(content.trim()).find();
  }

  public List<Reason> extractReasons(Message msg) {
    List<Reason> reasons = new ArrayList<Reason>();
    Set<Target> targets = new HashSet<Target>();
    Map<String, Target> alreadyFetched = new HashMap<String, Target>();
    boolean hasOverride = msg.content.contains("/combine");
    Matcher m = pattern.matcher(msg.content);
    while (m.find()) {
      String target = m.group(1).toLowerCase();
      String action = m.group(2);
      if (blacklist.contains(target)) {
        continue;
      }
      Target t = alreadyFetched.get(target);
      if (t == null) {
        t = Datastore.instance().getOrCreateTarget(msg.channel, target);
        alreadyFetched.put(target, t);
      }
      Action a = action.equals("--") ? Action.MINUSMINUS : Action.PLUSPLUS;
      if (targets.contains(t) && !hasOverride) {
        continue;
      } else {
        targets.add(t);
      }
      reasons.add(t.takeAction(msg.member, a, msg.content));
    }
    // Do a batch-save at the end.
    List<Serializable> toSave = new ArrayList<Serializable>(targets);
    toSave.addAll(reasons);
    Datastore.instance().putAll(toSave);
    return reasons;
  };
}