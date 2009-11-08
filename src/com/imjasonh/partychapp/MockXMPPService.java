package com.imjasonh.partychapp;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.Message;
import com.google.appengine.api.xmpp.Presence;
import com.google.appengine.api.xmpp.SendResponse;
import com.google.appengine.api.xmpp.XMPPService;
import com.google.appengine.api.xmpp.SendResponse.Status;
import com.google.appengine.repackaged.com.google.common.collect.Lists;

public class MockXMPPService implements XMPPService {
  public List<Message> messages = Lists.newArrayList();
  public List<JID> invited = Lists.newArrayList();

  public Presence getPresence(JID jabberId) {
    throw new RuntimeException("you should use the 2-arg version of getPresence instead");
  }

  public Presence getPresence(JID jabberId, JID fromJid) {
    return new Presence(false);
  }

  public Message parseMessage(HttpServletRequest arg0) throws IOException {
    throw new RuntimeException("not implemented in mock");
  }

  public void sendInvitation(JID jabberId) {
    invited.add(jabberId);
  }

  public void sendInvitation(JID jabberId, JID fromJid) {
    invited.add(jabberId);
  }

  public SendResponse sendMessage(Message message) {
    messages.add(message);
    SendResponse response = new SendResponse();
    for (JID jid : message.getRecipientJids()) {
      response.addStatus(jid, Status.SUCCESS);
    }
    return response;
  }
}
