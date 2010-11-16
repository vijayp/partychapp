package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.testing.FakeDatastore;

public class ToggleLoggingHandlerTest extends CommandHandlerTestCase {
  ToggleLoggingHandler handler = new ToggleLoggingHandler();
  
  public void testMatches() {
    assertTrue(handler.matches(Message.createForTests("/toggle-logging")));
    assertTrue(handler.matches(Message.createForTests(" /toggle-logging")));
    assertTrue(handler.matches(Message.createForTests("/togglelogging")));
    assertTrue(handler.matches(Message.createForTests("/toggle_logging")));
  }

  public void testToggling() {
    Channel channel = FakeDatastore.fakeChannel();
    assertFalse(channel.isLoggingDisabled());
    handler.doCommand(Message.createForTests("/toggle-logging"));
    assertTrue(channel.isLoggingDisabled());
    assertEquals(1, xmpp.messages.size());
    assertEquals(
        "_neil has disabled logging._", xmpp.messages.get(0).getBody());
    
    xmpp.messages.clear();
    handler.doCommand(Message.createForTests("/toggle-logging"));
    assertEquals(1, xmpp.messages.size());
    assertEquals(
        "_neil has enabled logging._", xmpp.messages.get(0).getBody());    
    assertFalse(channel.isLoggingDisabled());
  }
  
  public void testMessagesAreCleared() {
    Channel channel = FakeDatastore.fakeChannel();
    assertFalse(channel.isLoggingDisabled());
    FakeDatastore.fakeChannel().
        getMemberByAlias("jason").addToLastMessages("hack the gibson");
    handler.doCommand(Message.createForTests("/toggle-logging"));
    assertTrue(channel.isLoggingDisabled());
    assertTrue(FakeDatastore.fakeChannel().
        getMemberByAlias("jason").getLastMessages().isEmpty());
  }
}
