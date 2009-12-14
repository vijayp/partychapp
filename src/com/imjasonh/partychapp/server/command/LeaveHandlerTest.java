package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.FakeDatastore;
import com.imjasonh.partychapp.Message;

public class LeaveHandlerTest extends CommandHandlerTest {
  LeaveHandler handler = new LeaveHandler();

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
    assertNull(FakeDatastore.fakeChannel().getMemberByAlias("neil"));
  }
}
