package com.imjasonh.partychapp.server.command;

import com.google.appengine.api.xmpp.JID;
import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Member;

public interface CommandHandler {

  public void doCommand(String content, JID userJID, JID serverJID, Member member, Channel channel);
}
