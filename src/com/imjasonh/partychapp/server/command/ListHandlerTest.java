package com.imjasonh.partychapp.server.command;

import junit.framework.TestCase;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.FakeDatastore;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.MockXMPPService;
import com.imjasonh.partychapp.server.SendUtil;

public class ListHandlerTest extends TestCase {
  ListHandler handler = new ListHandler();
  MockXMPPService xmpp = new MockXMPPService();
  
  public void setUp() {
    Datastore.setInstance(new FakeDatastore());
    SendUtil.setXMPP(xmpp);
  }
  
  public void testMatches() {
    assertTrue(handler.matches(Message.createForTests("/list")));
    assertTrue(handler.matches(Message.createForTests(" /list")));
    assertFalse(handler.matches(Message.createForTests("use /list to tell you who's in the room")));
    assertTrue(handler.matches(Message.createForTests("/list args")));
  }

  public void testCommand() {
    handler.doCommand(Message.createForTests("/list"));
    assertEquals(1, xmpp.messages.size());
    assertTrue(xmpp.messages.get(0).getBody().startsWith("Listing members of"));
  }
}
