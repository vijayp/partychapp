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
  
  private Pattern pattern;
  
  SlashCommand(String pattern) {
    this.pattern = Pattern.compile("^/" + pattern);
  }

  /**
   * @return the matcher for reading any groups off of, or null if there was no
   *         match.
   */
  protected Matcher getMatcher(Message msg) {
    Matcher m = pattern.matcher(msg.content.trim());
    return m.find() ? m : null;
  }

  @Override
  public boolean matches(Message msg) {
    return getMatcher(msg) != null;
  }
}
