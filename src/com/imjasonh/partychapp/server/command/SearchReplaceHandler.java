package com.imjasonh.partychapp.server.command;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.Message.MessageType;
import com.imjasonh.partychapp.ppb.PlusPlusBot;

public class SearchReplaceHandler implements CommandHandler {
  private static Pattern pattern =
      Pattern.compile("^(" + AliasHandler.ALIAS_REGEX + ": )?s/([^/]+)/([^/]*)(/?)(g?)$");

  PlusPlusBot ppb = new PlusPlusBot();
  PPBHandler ppbHandler = new PPBHandler();

  private void sendNoMatchError(Message msg) {
    msg.channel.broadcastIncludingSender("No message found that matches that pattern.");
  }
  
  public void doCommand(Message msg) {
    boolean isSuggestion = false;
    List<String> lastMessages = Lists.newArrayList(msg.member.getLastMessages()); 
    String correctionPrefix = "_" + msg.member.getAlias() + " meant ";

    msg.member.addToLastMessages(msg.content);
    msg.channel.put();
    msg.channel.broadcast((msg.member.getAliasPrefix() + msg.content), msg.member);

    Matcher m = pattern.matcher(msg.content.trim());
    if (!m.matches()) {
      sendNoMatchError(msg);
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
        msg.channel.broadcastIncludingSender("No member named '" + otherAlias + "' found");
        return;
      } else if (other != msg.member) {
        lastMessages = other.getLastMessages();
        correctionPrefix = "_" + msg.member.getAlias() + " thinks " + otherAlias +
            " meant ";
        isSuggestion = true;
      }
    }

    String messageToChange = null;
    // TODO(nsanch): should I do anything special since this is user-supplied?
    Pattern p = Pattern.compile(toReplace);
    for (String curr : lastMessages) {
      if (p.matcher(curr).find()) {
        messageToChange = curr;
        break;
      }
    }
    if (messageToChange == null) {
      sendNoMatchError(msg);
      return;
    }
    
    String after = null;
    if (replaceAll) {
      after = messageToChange.replaceAll(toReplace, replacement);
    } else {
      after = messageToChange.replaceFirst(toReplace, replacement);
    }

    if (!isSuggestion) {
      Message originalMsg = new Message(messageToChange, msg.userJID,
                                        msg.serverJID, msg.member, msg.channel, null, MessageType.XMPP);
      if (ppbHandler.matches(originalMsg)) {
        ppbHandler.undoEarlierMessage(originalMsg);
      }
      Message afterMsg = new Message(after, msg.userJID, msg.serverJID, msg.member, msg.channel, null, MessageType.XMPP);
      if (ppbHandler.matches(afterMsg)) {
        ppbHandler.doCommandAsCorrection(afterMsg);
      } else {
        msg.channel.broadcastIncludingSender(correctionPrefix + after + "_");
      }
      msg.member.addToLastMessages(after);
      msg.channel.put();
    } else {
      msg.channel.broadcastIncludingSender(correctionPrefix + after + "_");
    }
  }

  public String documentation() {
    return "search and replace handler - use s/foo/bar to replace foo with bar";
  }

  public boolean matches(Message msg) {
    return pattern.matcher(msg.content.trim()).matches();
  }

}
