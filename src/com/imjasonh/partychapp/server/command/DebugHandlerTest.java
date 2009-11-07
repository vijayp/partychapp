package com.imjasonh.partychapp.server.command;

import junit.framework.TestCase;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.FakeDatastore;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.MockXMPPService;
import com.imjasonh.partychapp.server.SendUtil;

public class DebugHandlerTest extends TestCase {
  DebugHandler handler = new DebugHandler();
  private MockXMPPService xmpp = new MockXMPPService();
  
  public void setUp() {
    Datastore.setInstance(new FakeDatastore());
    SendUtil.setXMPP(xmpp);
  }
  
  public void testMatches() {
    assertTrue(handler.matches(Message.createForTests("/debug sequenceIds")));
    assertTrue(handler.matches(Message.createForTests("/debug clear")));
    assertTrue(handler.matches(Message.createForTests("/debug unknown-command")));
    assertTrue(handler.matches(Message.createForTests("/debug")));
    assertTrue(handler.matches(Message.createForTests(" /debug")));
  }
  
  public void testSequenceIds() {
    handler.doCommand(Message.createForTests("/debug sequenceIds"));
    assertTrue(FakeDatastore.fakeChannel().getMemberByAlias("neil").debugOptions().isEnabled("sequenceIds"));
    
    assertEquals(1, xmpp.messages.size());
    assertEquals("enabling sequenceIds for you",
                 xmpp.messages.get(0).getBody());
  }

  public void testClear() {
    handler.doCommand(Message.createForTests("/debug sequenceIds"));
    assertTrue(FakeDatastore.fakeChannel().getMemberByAlias("neil").debugOptions().isEnabled("sequenceIds"));
    handler.doCommand(Message.createForTests(" /debug clear"));
    assertFalse(FakeDatastore.fakeChannel().getMemberByAlias("neil").debugOptions().isEnabled("sequenceIds"));
    
    assertEquals(2, xmpp.messages.size());
    assertEquals("enabling sequenceIds for you",
                 xmpp.messages.get(0).getBody());
    assertEquals("clearing all debug options",
                 xmpp.messages.get(1).getBody());
  }
  
  public void testList() {
    handler.doCommand(Message.createForTests("/debug sequenceIds"));
    assertTrue(FakeDatastore.fakeChannel().getMemberByAlias("neil").debugOptions().isEnabled("sequenceIds"));
    handler.doCommand(Message.createForTests(" /debug"));
    assertTrue(FakeDatastore.fakeChannel().getMemberByAlias("neil").debugOptions().isEnabled("sequenceIds"));
    
    assertEquals(2, xmpp.messages.size());
    assertEquals("enabling sequenceIds for you",
                 xmpp.messages.get(0).getBody());
    assertEquals("Your current debug options are: [sequenceIds]",
                 xmpp.messages.get(1).getBody());    
  }
}
