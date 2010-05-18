package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.DebuggingOptions.Option;
import com.imjasonh.partychapp.testing.FakeDatastore;

public class DebugHandlerTest extends CommandHandlerTestCase {
  DebugHandler handler = new DebugHandler();
  
  public void testMatches() {
    assertTrue(handler.matches(Message.createForTests("/debug sequenceIds")));
    assertTrue(handler.matches(Message.createForTests("/debug clear")));
    assertTrue(handler.matches(Message.createForTests("/debug unknown-command")));
    assertTrue(handler.matches(Message.createForTests("/debug")));
    assertTrue(handler.matches(Message.createForTests(" /debug")));
  }
  
  public void testSequenceIds() {
    handler.doCommand(Message.createForTests("/debug sequenceIds"));
    assertTrue(FakeDatastore.fakeChannel().getMemberByAlias("neil")
        .debugOptions().isEnabled(Option.SEQUENCE_IDS));
    
    assertEquals(1, xmpp.messages.size());
    assertEquals("enabling sequenceIds for you",
                 xmpp.messages.get(0).getBody());
  }
  
  public void testErrorNotifications() {
    handler.doCommand(Message.createForTests("/debug errorNotifications"));
    assertTrue(FakeDatastore.fakeChannel().getMemberByAlias("neil")
        .debugOptions().isEnabled(Option.ERROR_NOTIFICATIONS));
    
    assertEquals(1, xmpp.messages.size());
    assertEquals("enabling errorNotifications for you",
                 xmpp.messages.get(0).getBody());
  }  

  public void testClear() {
    handler.doCommand(Message.createForTests("/debug sequenceIds"));
    assertTrue(FakeDatastore.fakeChannel().getMemberByAlias("neil")
        .debugOptions().isEnabled(Option.SEQUENCE_IDS));
    handler.doCommand(Message.createForTests(" /debug clear"));
    assertFalse(FakeDatastore.fakeChannel().getMemberByAlias("neil")
        .debugOptions().isEnabled(Option.SEQUENCE_IDS));
    
    assertEquals(2, xmpp.messages.size());
    assertEquals("enabling sequenceIds for you",
                 xmpp.messages.get(0).getBody());
    assertEquals("clearing all debug options",
                 xmpp.messages.get(1).getBody());
  }
  
  public void testList() {
    handler.doCommand(Message.createForTests("/debug sequenceIds"));
    assertTrue(FakeDatastore.fakeChannel().getMemberByAlias("neil")
        .debugOptions().isEnabled(Option.SEQUENCE_IDS));
    handler.doCommand(Message.createForTests("/debug errorNotifications"));
    assertTrue(FakeDatastore.fakeChannel().getMemberByAlias("neil")
        .debugOptions().isEnabled(Option.ERROR_NOTIFICATIONS));
    handler.doCommand(Message.createForTests(" /debug"));
    assertTrue(FakeDatastore.fakeChannel().getMemberByAlias("neil")
        .debugOptions().isEnabled(Option.SEQUENCE_IDS));
    assertTrue(FakeDatastore.fakeChannel().getMemberByAlias("neil")
        .debugOptions().isEnabled(Option.ERROR_NOTIFICATIONS));
    
    assertEquals(3, xmpp.messages.size());
    assertEquals("enabling sequenceIds for you",
                 xmpp.messages.get(0).getBody());
    assertEquals("enabling errorNotifications for you",
                 xmpp.messages.get(1).getBody());
    assertEquals(
        "Your current debug options are: [sequenceIds, errorNotifications]",
        xmpp.messages.get(2).getBody());    
  }
}
