package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;

public class MeHandlerTest extends CommandHandlerTest {
  MeHandler handler = new MeHandler();
  
  public void testMatches() {
    assertTrue(handler.matches(Message.createForTests("/me x")));
    assertTrue(handler.matches(Message.createForTests("/me")));
    assertTrue(handler.matches(Message.createForTests("        /me wants a cookie")));
    assertFalse(handler.matches(Message.createForTests("x /me")));
    assertFalse(handler.matches(Message.createForTests("/meanie")));
  }
  
  public void testCommand() {
    handler.doCommand(Message.createForTests("/me xs"));
    assertEquals(1, xmpp.messages.size());
    assertEquals("_neil xs_", xmpp.messages.get(0).getBody());
  }
}