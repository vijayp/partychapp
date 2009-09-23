package com.imjasonh.partychapp;

import com.google.appengine.api.xmpp.JID;

public class Message {
  public static Message createForTests(String content) {
    Channel c = FakeDatastore.instance().fakeChannel();
    JID userJID = new JID("neil@gmail.com");
    return new Message(content,
                       userJID,
                       new JID("pancake@partychat.appspotchat.com"),
                       c.getMemberByJID(userJID),
                       c);
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
  
  public String toString() {
    return "[Message: content = '" + content + "', userJID = " + userJID
        + ", serverJID = " + serverJID +
        ", member = " + member +
        ", channel = " + channel;
  }
}
