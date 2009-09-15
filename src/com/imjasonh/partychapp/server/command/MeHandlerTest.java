package com.imjasonh.partychapp.server.command;

import junit.framework.TestCase;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.FakeDatastore;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.MockXMPPService;
import com.imjasonh.partychapp.server.SendUtil;

public class MeHandlerTest extends TestCase {
  MeHandler handler = new MeHandler();
  MockXMPPService xmpp = new MockXMPPService();
  
  public void setUp() {
    Datastore.setInstance(new FakeDatastore());
    SendUtil.setXMPP(xmpp);
  }
  
  public void testMatches() {
    assertTrue(handler.matches(Message.createForTests("/me x")));
    assertTrue(handler.matches(Message.createForTests("/me")));
    assertTrue(handler.matches(Message.createForTests("        /me wants a cookie")));
    assertFalse(handler.matches(Message.createForTests("x /me")));
  }
  
  public void testScore() {
    handler.doCommand(Message.createForTests("/me xs"));
    assertEquals(1, xmpp.messages.size());
    assertEquals("_neil xs_", xmpp.messages.get(0).getBody());
  }
}