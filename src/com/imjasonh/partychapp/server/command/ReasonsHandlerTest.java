package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.FakeDatastore;
import com.imjasonh.partychapp.Message;

import junit.framework.TestCase;

public class ReasonsHandlerTest extends TestCase {
  ReasonsHandler handler = new ReasonsHandler();
  
  public void setUp() {
    Datastore.setInstance(new FakeDatastore());
  }
  
  public void testMatches() {
    assertTrue(handler.matches(Message.createForTests("/reasons x")));
    assertTrue(handler.matches(Message.createForTests(" /reasons x")));
    assertTrue(handler.matches(Message.createForTests("/reasons xyz")));
    assertTrue(handler.matches(Message.createForTests("/reasons a_b-c.d")));
    assertFalse(handler.matches(Message.createForTests("x /reasons")));
  }
}
