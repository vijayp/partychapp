package com.imjasonh.partychapp;

import com.google.appengine.api.xmpp.JID;

public class Message {
  public static Message createForTests(String content) {
    JID userJID = new JID("neil@gmail.com");
    return new Message(content,
                       userJID,
                       new JID("pancake@partychat.appspotchat.com"),
                       FakeDatastore.instance().fakeChannel().getMemberByJID(userJID),
                       FakeDatastore.instance().fakeChannel());
  }

  public Message(String content, JID userJID, JID serverJID, Member member,
          Channel channel) {
    this.content = content;
    this.userJID = userJID;
    this.serverJID = serverJID;
    this.member = member;
    this.channel = channel;
  }

  public final String content;
  public final JID userJID;
  public final JID serverJID;
  public Member member;
  public Channel channel;
}
