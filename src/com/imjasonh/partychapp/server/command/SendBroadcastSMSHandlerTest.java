package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;

import junit.framework.TestCase;

public class SendBroadcastSMSHandlerTest extends TestCase {
  private SendBroadcastSMSHandler handler = new SendBroadcastSMSHandler();
  
  public void testMatches() {
    assertTrue(handler.matches(
                  Message.createForTests("/broadcast-sms trying to send a broadcast sms")));
  }
}
