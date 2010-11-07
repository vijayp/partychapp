package com.imjasonh.partychapp.server.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.imjasonh.partychapp.Message;

/**
 * Superclass for commands that use regexes starting with / to match.
 * 
 * @author kushaldave@gmail.com
 */
abstract class SlashCommand implements CommandHandler {

  private final Pattern pattern;

  SlashCommand(String name, String... otherNames) {
    StringBuilder sb = new StringBuilder("^/(?:");
    sb.append(name);
    for (String otherName : otherNames) {
      sb.append("|").append(otherName);
    }
    sb.append(")(\\s.*)?$");

    this.pattern = Pattern.compile(sb.toString());
  }

  /**
   * Subclass do the actual work here, including validating argument
   * as needed.
   */
  abstract void doCommand(Message msg, String argument);

  public void doCommand(Message msg) {
    Matcher matcher = getMatcher(msg);
    String argument = matcher.group(1);
    if (argument != null) {
      argument = argument.trim();
    }
    doCommand(msg, argument);
  }

  public boolean matches(Message msg) {
    return getMatcher(msg) != null;
  }

  /**
   * @return the matcher for reading any groups off of, or null if there was no
   *         match.
   */
  private Matcher getMatcher(Message msg) {
    Matcher m = pattern.matcher(msg.content.trim());
    return m.matches() ? m : null;
  }
}
