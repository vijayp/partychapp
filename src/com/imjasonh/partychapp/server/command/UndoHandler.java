package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

public class UndoHandler extends SlashCommand {
  private PPBHandler ppbHandler = new PPBHandler();
  
  public UndoHandler() {
    super("undo");
  }
  
  @Override
  void doCommand(Message msg, String argument, HttpServletResponse resp) {
    msg.channel.broadcast(msg.member.getAliasPrefix() + msg.content, msg.member, resp);
    List<String> lastMessages = msg.member.getLastMessages();
    if (lastMessages.isEmpty()) {
      String reply = "no last message to undo!";
      msg.channel.broadcastIncludingSender(reply, resp);
      return;
    }

    String toUndo = lastMessages.get(0);
    Message originalMsg =
        Message.Builder.basedOn(msg).setContent(toUndo).build();
    ppbHandler.undoEarlierMessage(originalMsg);
  }

  public String documentation() {
    return "/undo - undo the pluspluses and minusminuses from your last message";
  }
}