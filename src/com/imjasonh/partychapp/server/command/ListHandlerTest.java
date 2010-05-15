package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;

public class ListHandlerTest extends CommandHandlerTestCase {
  ListHandler handler = new ListHandler();
  
  public void testMatches() {
    assertTrue(handler.matches(Message.createForTests("/list")));
    assertTrue(handler.matches(Message.createForTests(" /list")));
    assertFalse(handler.matches(Message.createForTests("use /list to tell you who's in the room")));
    assertTrue(handler.matches(Message.createForTests("/list args")));
  }

  public void testCommand() {
    handler.doCommand(Message.createForTests("/list"));
    assertEquals(1, xmpp.messages.size());
    assertEquals("Listing members of 'pancake'\n" +
                 "* akshay (akshay@q00p.net)\n" +
                 "* david (david@gmail.com)\n" +
                 "* jason (jason@gmail.com)\n" +
                 "* kushal (kushal@kushaldave.com)\n" +
                 "* neil (neil@gmail.com)",
                 xmpp.messages.get(0).getBody());

  }
}
