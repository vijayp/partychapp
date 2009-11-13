package com.imjasonh.partychapp;

import com.google.appengine.api.xmpp.JID;

public class Message {
  public enum MessageType { EMAIL, XMPP };
  
  public static Message createForTests(String content) {
    Channel c = FakeDatastore.fakeChannel();
    JID userJID = new JID("neil@gmail.com");
    return new Message(content,
                       userJID,
                       new JID("pancake@partychapp.appspotchat.com"),
                       c.getMemberByJID(userJID),
                       c, MessageType.XMPP);
  }

  public Message(String content, JID userJID, JID serverJID, Member member,
          Channel channel, MessageType messageType) {
    this.content = content;
    this.userJID = userJID;
    this.serverJID = serverJID;
    this.member = member;
    this.channel = channel;
    this.messageType = messageType;
  }
  
  public final String content;
  public final JID userJID;
  public JID serverJID;
  public Member member;
  public Channel channel;
  public MessageType messageType;
  
  public String toString() {
    return "[Message: content = '" + content + "', userJID = " + userJID
        + ", serverJID = " + serverJID +
        ", member = " + member +
        ", channel = " + channel;
  }
}
