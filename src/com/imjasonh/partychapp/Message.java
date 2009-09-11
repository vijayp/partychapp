package com.imjasonh.partychapp;

import com.google.appengine.api.xmpp.JID;

public class Message {
	public Message(String content, JID userJID, JID serverJID, Member member, Channel channel) {
		this.content = content;
		this.userJID = userJID;
		this.serverJID = serverJID;
		this.member = member;
		this.channel = channel;
	}
	
	// for tests only
	public Message(String content) {
		this.content = content;
		this.userJID = null;
		this.serverJID = null;
		this.member = null;
		this.channel = null;
	}
	
	public final String content;
	public final JID userJID;
	public final JID serverJID;
	public Member member;
	public Channel channel;
}
