package com.imjasonh.partychapp;

import com.google.appengine.api.xmpp.JID;

import com.imjasonh.partychapp.testing.FakeDatastore;

public class Message {
  public enum MessageType { EMAIL, XMPP, SMS }
  
  public static Message createForTests(String content) {
    Channel c = FakeDatastore.fakeChannel();
    JID userJID = new JID("neil@gmail.com");
    return new Message(content,
                       userJID,
                       new JID("pancake@partychapp.appspotchat.com"),
                       c.getMemberByJID(userJID),
                       c, null, MessageType.XMPP);
  }

  public Message(String content, JID userJID, JID serverJID, Member member,
          Channel channel, String phoneNumber, MessageType messageType) {
    this.content = content;
    this.userJID = userJID;
    this.serverJID = serverJID;
    this.member = member;
    this.channel = channel;
    this.messageType = messageType;
    this.phoneNumber = phoneNumber;
  }
  
  public final String content;
  public final JID userJID;
  public JID serverJID;
  public Member member;
  public Channel channel;
  public MessageType messageType;
  public String phoneNumber;
  
  @Override
  public String toString() {
    return "[Message: content = '" + content + "', userJID = " + userJID
        + ", serverJID = " + serverJID +
        ", member = " + member +
        ", channel = " + channel + 
        ", type = " + messageType +
        ", phone = " + (phoneNumber != null ? phoneNumber : "null");
  }
}
