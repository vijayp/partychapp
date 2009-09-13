package com.imjasonh.partychapp.server.command;

import junit.framework.TestCase;

import com.imjasonh.partychapp.Message;

public class ListHandlerTest extends TestCase {
  public void testMatches() {
    CommandHandler handler = new ListHandler();
    assertTrue(handler.matches(Message.createForTests("/list")));
    assertTrue(handler.matches(Message.createForTests(" /list")));
    assertFalse(handler.matches(Message.createForTests("use /list to tell you who's in the room")));
    assertFalse(handler.matches(Message.createForTests("/list args")));
  }
}
