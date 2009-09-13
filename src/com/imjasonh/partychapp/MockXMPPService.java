package com.imjasonh.partychapp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.Message;
import com.google.appengine.api.xmpp.Presence;
import com.google.appengine.api.xmpp.SendResponse;
import com.google.appengine.api.xmpp.XMPPService;

public class MockXMPPService implements XMPPService {
  public List<Message> messages = new ArrayList<Message>();
  
  
  @Override
  public Presence getPresence(JID jabberId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Presence getPresence(JID jabberId, JID fromJid) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Message parseMessage(HttpServletRequest arg0) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void sendInvitation(JID jabberId) {
    // TODO Auto-generated method stub

  }

  @Override
  public void sendInvitation(JID jabberId, JID fromJid) {
    // TODO Auto-generated method stub

  }

  @Override
  public SendResponse sendMessage(Message message) {
    messages.add(message);
    return null;
  }
}
