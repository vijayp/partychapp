package com.imjasonh.partychapp;

import com.google.appengine.api.xmpp.JID;

import com.imjasonh.partychapp.testing.FakeDatastore;

public class Message {
  public enum MessageType { EMAIL, XMPP, SMS }
  
  public static Message createForTests(String content) {
    return createForTests(content, MessageType.XMPP);
  }
  public static Message createForTests(
          String content, MessageType messageType) {
    JID userJID = new JID("neil@gmail.com");
    Channel c = FakeDatastore.fakeChannel();
    
    return new Builder()
      .setContent(content)
      .setUserJID(userJID)
      .setServerJID(new JID("pancake@partychapp.appspotchat.com"))
      .setMember(c.getMemberByJID(userJID))
      .setChannel(c)
      .setMessageType(messageType)
      .build();
  }

  private Message(Builder builder) {
    this.content = builder.content;
    this.userJID = builder.userJID;
    this.serverJID = builder.serverJID;
    this.member = builder.member;
    this.channel = builder.channel;
    this.messageType = builder.messageType;
  }
  
  public final String content;
  public final JID userJID;
  public final JID serverJID;
  public Member member;
  public Channel channel;
  public final MessageType messageType;
  
  public static class Builder {
    private String content;
    private JID userJID;
    private JID serverJID;
    private Member member;
    private Channel channel;
    private MessageType messageType;

    /**
     * Creates a new Builder that is pre-populated with all of the fields from
     * {@code other} _except_ for content.
     */
    public static Builder basedOn(Message other) {
      return new Builder()
          .setUserJID(other.userJID)
          .setServerJID(other.serverJID)
          .setMember(other.member)
          .setChannel(other.channel)
          .setMessageType(other.messageType);
    }
    
    public Builder setContent(String content) {
      this.content = content;
      return this;
    }

    public Builder setUserJID(JID userJID) {
      this.userJID = userJID;
      return this;
    }

    public Builder setServerJID(JID serverJID) {
      this.serverJID = serverJID;
      return this;
    }

    public Builder setMember(Member member) {
      this.member = member;
      return this;
    }

    public Builder setChannel(Channel channel) {
      this.channel = channel;
      return this;
    }

    public Builder setMessageType(MessageType messageType) {
      this.messageType = messageType;
      return this;
    }

    public Message build() {
      return new Message(this);
    }
  }
  
  @Override
  public String toString() {
    return "[Message: content = '" + content + "', userJID = " + userJID
        + ", serverJID = " + serverJID +
        ", member = " + member +
        ", channel = " + channel + 
        ", type = " + messageType;
  }
}
