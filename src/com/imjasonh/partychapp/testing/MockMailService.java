package com.imjasonh.partychapp.testing;

import java.io.IOException;
import java.util.List;

import com.google.appengine.api.mail.MailService;
import com.google.common.collect.Lists;

public class MockMailService implements MailService {
  public final List<Message> sentMessages = Lists.newArrayList();
  boolean shouldThrowException = false;
  
  public void setThrowException() {
    shouldThrowException = true;
  }
  
  public void send(Message message) throws IOException {
    if (shouldThrowException) {
      throw new IOException();
    }
    sentMessages.add(message);
  }

  public void sendToAdmins(Message message) {
    throw new RuntimeException("sendToAdmins not implemented in the fake");
  }

}
