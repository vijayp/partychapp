package com.imjasonh.partychapp.server;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.appengine.api.xmpp.JID;
import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Member;

public enum Command {

  LEAVE(Pattern.compile("^/(leave|exit)"), "/leave - leave the room", new LeaveHandler()),

  LIST(Pattern.compile("^/(list|names)"), "/list - show members of room", new ListHandler()),

  HELP(Pattern.compile("^/(help|commands)"), "/help shows this", new HelpHandler()),

  ALIAS(Pattern.compile("^/(alias|rename) [a-zA-Z0-9]+"),
      "/alias - rename yourself",
      new AliasHandler()), ;

  private final Pattern pattern;
  private final String documentation;
  private final CommandHandler commandHandler;

  private Command(Pattern pattern, String documentation, CommandHandler commandHandler) {
    this.pattern = pattern;
    this.documentation = documentation;
    this.commandHandler = commandHandler;
  }

  public String getDocumentation() {
    return documentation;
  }

  public boolean matches(String content) {
    Matcher matcher = pattern.matcher(content);
    return matcher.matches();
  }

  public void run(String content, JID userJID, JID serverJID, Member member, Channel channel) {
    commandHandler.doCommand(content, userJID, serverJID, member, channel);
  }
}
