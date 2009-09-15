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
    this.pattern = Pattern.compile("/" + pattern);
  }

  protected Matcher getMatcher(Message msg) {
    return pattern.matcher(msg.content.trim()); 
  }
  
  public boolean matches(Message msg) {
    return getMatcher(msg).matches();
  }
}
