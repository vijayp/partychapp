package com.imjasonh.partychapp;

import com.google.appengine.api.xmpp.JID;

public class Message {
  public static Message createForTests(String content) {
    return new Message(content);
  }

  public Message(String content, JID userJID, JID serverJID, Member member,
          Channel channel) {
    this.content = content;
    this.userJID = userJID;
    this.serverJID = serverJID;
    this.member = member;
    this.channel = channel;
  }

  // for tests only. TODO(nsanch): this blows.
  private Message(String content) {
    this.content = content;
    this.userJID = new JID("neil@gmail.com");
    this.serverJID = new JID("pancake@partychat.appspotchat.com");
    this.channel = Datastore.instance().getChannelByName("pancake");
    this.member = this.channel.getMemberByJID(userJID);
  }

  public final String content;
  public final JID userJID;
  public final JID serverJID;
  public Member member;
  public Channel channel;
}
