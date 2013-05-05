package com.imjasonh.partychapp.testing;

import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.Message;
import com.google.appengine.api.xmpp.Presence;
import com.google.appengine.api.xmpp.PresenceBuilder;
import com.google.appengine.api.xmpp.PresenceShow;
import com.google.appengine.api.xmpp.PresenceType;
import com.google.appengine.api.xmpp.SendResponse;
import com.google.appengine.api.xmpp.Subscription;
import com.google.appengine.api.xmpp.XMPPService;
import com.google.appengine.api.xmpp.SendResponse.Status;
import com.google.common.collect.Lists;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

public class MockXMPPService implements XMPPService {
  public List<Message> messages = Lists.newArrayList();
  public List<JID> invited = Lists.newArrayList();

  @Override
  public Presence getPresence(JID jabberId) {
    throw new RuntimeException("you should use the 2-arg version of getPresence instead");
  }

  @Override
  public Presence getPresence(JID jabberId, JID fromJid) {
    return new PresenceBuilder().withPresenceType(PresenceType.UNAVAILABLE).build();
  }

  @Override
  public Message parseMessage(HttpServletRequest arg0) {
    throw new RuntimeException("not implemented in mock");
  }

  @Override
  public void sendInvitation(JID jabberId) {
    invited.add(jabberId);
  }

  @Override
  public void sendInvitation(JID jabberId, JID fromJid) {
    invited.add(jabberId);
  }

  @Override
  public SendResponse sendMessage(Message message) {
    messages.add(message);
    SendResponse response = new SendResponse();
    for (JID jid : message.getRecipientJids()) {
      response.addStatus(jid, Status.SUCCESS);
    }
    return response;
  }

  @Override
  public Presence parsePresence(HttpServletRequest req) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Subscription parseSubscription(HttpServletRequest req) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void sendPresence(
      JID jabberId, PresenceType type, PresenceShow show, String status) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void sendPresence(
      JID jabberId,
      PresenceType type,
      PresenceShow show,
      String status,
      JID fromJid) {
    throw new UnsupportedOperationException();
  }

@Override
public List<Presence> getPresence(Iterable<JID> jabberIds) {
	// TODO Auto-generated method stub
	return null;
}

@Override
public List<Presence> getPresence(Iterable<JID> jabberIds, JID fromJid) {
	// TODO Auto-generated method stub
	return null;
}
}
