package com.imjasonh.partychapp.server.command;

import com.google.common.collect.Lists;

import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.ppb.PlusPlusBot;
import com.imjasonh.partychapp.server.PartychappServlet;

import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.http.HttpServletResponse;

public class SearchReplaceHandler implements CommandHandler {
  private static final Logger logger =
      Logger.getLogger(SearchReplaceHandler.class.getName());

  private static Pattern pattern =
      Pattern.compile("^(" + AliasHandler.ALIAS_REGEX + ": )?s/([^/]+)/([^/]*)(/?)(g?)$");

  PlusPlusBot ppb = new PlusPlusBot();
  PPBHandler ppbHandler = new PPBHandler();

  private void sendNoMatchError(Message msg, HttpServletResponse resp) {
    msg.channel.broadcastIncludingSender("No message found that matches that pattern.", resp);
  }
  
  public void doCommand(Message msg) {
    doCommand(msg, null);
  }
  public void doCommand(Message msg, HttpServletResponse resp) {

    List<String> lastMessages = Lists.newArrayList(msg.member.getLastMessages()); 

    msg.member.addToLastMessages(msg.content);
    if (msg.channel.shouldDisableLogging == null || msg.channel.shouldDisableLogging) {
      logger.warning("cleared shoulddisablelogging for channel " + msg.channel.getName());
      msg.channel.shouldDisableLogging = new Boolean(false);
    }
    msg.channel.shouldDisableLogging = false;
    msg.channel.put();
    msg.channel.broadcast((msg.member.getAliasPrefix() + msg.content), msg.member);

    if (msg.channel.isLoggingDisabled()) {
      msg.channel.broadcastIncludingSender(
          "Search-and-replace is not supported if logging is disabled. You " +
          "can enable logging with the /togglelogging command or by visiting " +
          "the room's page at " + msg.channel.webUrl(),
            resp);
      return;
    }
    
    boolean isSuggestion = false;
    String correctionPrefix = msg.member.getAlias() + " meant _";
    
    Matcher m = pattern.matcher(msg.content.trim());
    if (!m.matches()) {
      sendNoMatchError(msg, resp);
      return;
    }

    String otherAlias = m.group(1);
    String toReplace = m.group(2);
    String replacement = m.group(3);
    String trailingSlash = m.group(4);
    boolean replaceAll = false;
    if (trailingSlash.isEmpty()) {
      replacement += m.group(5);
    } else {
      replaceAll = m.group(5).equals("g");
    }
    
    if (otherAlias != null && !otherAlias.isEmpty()) {
      otherAlias = otherAlias.substring(0, otherAlias.indexOf(":"));
      Member other = msg.channel.getMemberByAlias(otherAlias);
      if (other == null) {
        msg.channel.broadcastIncludingSender("No member named '" + otherAlias + "' found", resp);
        return;
      } else if (other != msg.member) {
        lastMessages = other.getLastMessages();
        correctionPrefix = msg.member.getAlias() + " thinks " + otherAlias +
            " meant _";
        isSuggestion = true;
      }
    }

    String messageToChange = null;
    Pattern p;
    try {
      p = Pattern.compile(toReplace);
    } catch (PatternSyntaxException err) {
      msg.channel.sendDirect("malformed search pattern", msg.member, resp);
      return;
    }
    for (String curr : lastMessages) {
      if (p.matcher(curr).find()) {
        messageToChange = curr;
        break;
      }
    }
    if (messageToChange == null) {
      sendNoMatchError(msg, resp);
      return;
    }
    
    String after = null;
    if (replaceAll) {
      after = messageToChange.replaceAll(toReplace, replacement);
    } else {
      after = messageToChange.replaceFirst(toReplace, replacement);
    }

    if (!isSuggestion) {
      Message originalMsg =
          Message.Builder.basedOn(msg).setContent(messageToChange).build();
      if (ppbHandler.matches(originalMsg)) {
        ppbHandler.undoEarlierMessage(originalMsg);
      }
      Message afterMsg =
          Message.Builder.basedOn(msg).setContent(after).build();
      if (ppbHandler.matches(afterMsg)) {
        ppbHandler.doCommandAsCorrection(afterMsg, resp);
      } else {
        msg.channel.broadcastIncludingSender(correctionPrefix + after + "_", resp);
      }
      msg.member.addToLastMessages(after);
      msg.channel.put();
    } else {
      msg.channel.broadcastIncludingSender(correctionPrefix + after + "_", resp);
    }
  }

  public String documentation() {
    return "search and replace handler - use s/foo/bar to replace foo with bar";
  }

  public boolean matches(Message msg) {
    return pattern.matcher(msg.content.trim()).matches();
  }

}
