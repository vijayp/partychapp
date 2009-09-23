package com.imjasonh.partychapp.server.command;

import junit.framework.TestCase;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.FakeDatastore;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.MockXMPPService;
import com.imjasonh.partychapp.server.SendUtil;

public class StatusHandlerTest extends TestCase {
  StatusHandler handler = new StatusHandler();
  MockXMPPService xmpp = new MockXMPPService();
  
  public void setUp() {
    Datastore.setInstance(new FakeDatastore());
    SendUtil.setXMPP(xmpp);
  }

  public void testMatches() {
    assertTrue(handler.matches(Message.createForTests("/status")));
    assertTrue(handler.matches(Message.createForTests(" /status")));
  }
  
  public void testInRoom() {
    handler.doCommand(Message.createForTests("/status"));
    assertEquals(1, xmpp.messages.size());
    assertEquals("You are currently in 'pancake' as 'neil'", xmpp.messages.get(0).getBody());
  }
}