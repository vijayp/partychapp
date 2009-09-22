package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.FakeDatastore;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.MockXMPPService;
import com.imjasonh.partychapp.server.SendUtil;

import junit.framework.TestCase;

public class LeaveHandlerTest extends TestCase {
  LeaveHandler handler = new LeaveHandler();
  MockXMPPService xmpp = new MockXMPPService();
  
  public void setUp() {
    Datastore.setInstance(new FakeDatastore());
    SendUtil.setXMPP(xmpp);
  }
  
  public void testMatches() {
    assertTrue(handler.matches(Message.createForTests("/leave")));
    assertTrue(handler.matches(Message.createForTests(" /leave")));
    assertFalse(handler.matches(Message.createForTests("use /leave to leave the room")));
  }

  public void testCommand() {
    handler.doCommand(Message.createForTests("/leave"));
    assertEquals(2, xmpp.messages.size());
    assertEquals("You have left the room 'pancake'",
                 xmpp.messages.get(0).getBody());
    assertEquals("neil has left the room (neil@gmail.com)",
                 xmpp.messages.get(1).getBody());

  }
}