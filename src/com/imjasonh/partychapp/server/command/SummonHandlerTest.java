package com.imjasonh.partychapp.server.command;

import junit.framework.TestCase;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.FakeDatastore;
import com.imjasonh.partychapp.MockMailService;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.MockXMPPService;
import com.imjasonh.partychapp.server.SendUtil;

public class SummonHandlerTest extends TestCase {
  SummonHandler handler = new SummonHandler();
  MockXMPPService xmpp = new MockXMPPService();
  MockMailService mailer = new MockMailService();
  
  public void setUp() {
    Datastore.setInstance(new FakeDatastore());
    SendUtil.setXMPP(xmpp);
    handler.setMailService(mailer);
  }
  
  public void testMatches() {
    assertTrue(handler.matches(Message.createForTests("/summon jason")));
    assertTrue(handler.matches(Message.createForTests(" /summon ")));
    assertTrue(handler.matches(Message.createForTests("/summon jason anything")));
  }
  
  public void testSummonSomeone() {
    handler.doCommand(Message.createForTests("/summon jason"));
    assertEquals(2, xmpp.messages.size());
    assertEquals("[\"neil\"] /summon jason", xmpp.messages.get(0).getBody());
    assertEquals("_neil summoned jason_", xmpp.messages.get(1).getBody());
    
    assertEquals(1, mailer.sentMessages.size());
    assertEquals("neil has summoned you to 'pancake'.",
                 mailer.sentMessages.get(0).getTextBody());
    assertEquals("You have been summoned to 'pancake'",
                 mailer.sentMessages.get(0).getSubject());
    assertEquals("partychat@gmail.com",
                 mailer.sentMessages.get(0).getSender());
  }
  
  public void testSummonUnknownAlias() {
    handler.doCommand(Message.createForTests("/summon fdsakfj"));
    assertEquals(2, xmpp.messages.size());
    assertEquals("[\"neil\"] /summon fdsakfj", xmpp.messages.get(0).getBody());
    assertEquals("Could not find member with alias 'fdsakfj'", xmpp.messages.get(1).getBody());
    
    assertEquals(0, mailer.sentMessages.size());
  }
  
  public void testException() {
    mailer.setThrowException();
    handler.doCommand(Message.createForTests("/summon jason"));
    assertEquals(2, xmpp.messages.size());
    assertEquals("[\"neil\"] /summon jason", xmpp.messages.get(0).getBody());
    assertEquals("Error while summoning 'jason' to room. Email may not have been sent.",
                 xmpp.messages.get(1).getBody());
  }
}
