package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;

public class AliasHandlerTest extends CommandHandlerTestCase {
  public void testAliasMatches() {
    AliasHandler handler = new AliasHandler();

    assertTrue(handler.matches(Message.createForTests("/alias nsanch")));
    assertTrue(handler.matches(Message.createForTests(" /alias with-a-space-in-front")));
    assertFalse(handler.matches(Message.createForTests("no you shouldn't handle this string with /alias")));
    assertTrue(handler.matches(Message.createForTests("/alias abcABC123-_*'")));
  }
}
