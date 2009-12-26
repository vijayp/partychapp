package com.imjasonh.partychapp.server.command;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.imjasonh.partychapp.Message;

public class BugHandler extends SlashCommand {
  private static final Logger LOG = Logger.getLogger(BugHandler.class.getName());

  public BugHandler() {
    super("bug");
  }
  
  @Override
  void doCommand(Message msg, String argument) {
    String summary = argument.trim();
    String comment = "Filed by user " + msg.member.getAlias() + " from channel " + msg.channel.getName();
    try {
      String uri = "http://code.google.com/p/partychapp/issues/entry?summary=" + URLEncoder.encode(summary, "UTF-8")
          + "&comment=" + URLEncoder.encode(comment, "UTF-8");
      msg.channel.sendDirect(uri, msg.member);
    } catch (UnsupportedEncodingException e) {
      LOG.log(Level.WARNING, "failed to encode /bug with argument " + argument, e);
    }
  }

  @Override
  public String documentation() {
    return "/bug - returns a link to a pre-populated form to file a bug.";
  }
}
