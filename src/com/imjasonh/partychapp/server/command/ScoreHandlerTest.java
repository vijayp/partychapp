package com.imjasonh.partychapp.server.command;

import junit.framework.TestCase;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.FakeDatastore;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.MockXMPPService;
import com.imjasonh.partychapp.server.SendUtil;

public class ScoreHandlerTest extends CommandHandlerTest {
  ScoreHandler handler = new ScoreHandler();
  
  public void testMatches() {
    assertTrue(handler.matches(Message.createForTests("/score x")));
    assertTrue(handler.matches(Message.createForTests(" /score x")));
    assertTrue(handler.matches(Message.createForTests("/score xyz")));
    assertTrue(handler.matches(Message.createForTests("/score a_b-c.d")));
    assertFalse(handler.matches(Message.createForTests("x /score")));
  }
  
  public void testScore() {
    PPBHandler ppb = new PPBHandler();
    ppb.doCommand(Message.createForTests("x++"));
    xmpp.messages.clear();
    
    handler.doCommand(Message.createForTests("/score x"));
    assertEquals(1, xmpp.messages.size());
    assertEquals("x: 1", xmpp.messages.get(0).getBody());
  }
}