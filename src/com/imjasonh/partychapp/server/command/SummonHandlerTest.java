package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.server.MailUtil;
import com.imjasonh.partychapp.testing.FakeDatastore;
import com.imjasonh.partychapp.testing.MockMailService;

public class SummonHandlerTest extends CommandHandlerTestCase {
  SummonHandler handler = new SummonHandler();
  MockMailService mailer = new MockMailService();
  
  @Override
  public void setUp() {
	super.setUp();
    MailUtil.setMailService(mailer);
  }
  
  public void testMatches() {
    assertTrue(handler.matches(Message.createForTests("/summon jason")));
    assertTrue(handler.matches(Message.createForTests(" /summon ")));
    assertTrue(handler.matches(Message.createForTests("/summon jason anything")));
  }
  
  public void testSummonSomeone() {
    handler.doCommand(Message.createForTests("/summon jason"));
    assertEquals(2, xmpp.messages.size());
    assertEquals("[neil] /summon jason", xmpp.messages.get(0).getBody());
    assertEquals("_neil summoned jason_", xmpp.messages.get(1).getBody());
    
    assertEquals(1, mailer.sentMessages.size());
    assertEquals("neil has summoned you to 'pancake'.",
                 mailer.sentMessages.get(0).getTextBody());
    assertEquals("You have been summoned to 'pancake'",
                 mailer.sentMessages.get(0).getSubject());
    assertEquals("pancake@partychapp.appspotmail.com",
                 mailer.sentMessages.get(0).getSender());
  }

  public void testSummonSomeoneWithMessage() {
	  handler.doCommand(Message.createForTests("/summon jason where is lauren?"));
	  assertEquals(2, xmpp.messages.size());
	  assertEquals("[neil] /summon jason where is lauren?", xmpp.messages.get(0).getBody());
	  assertEquals("_neil summoned jason_", xmpp.messages.get(1).getBody());

	  assertEquals(1, mailer.sentMessages.size());
	  assertEquals("neil has summoned you to 'pancake'.\n neil said: where is lauren?",
			  mailer.sentMessages.get(0).getTextBody());
	  assertEquals("You have been summoned to 'pancake'",
			  mailer.sentMessages.get(0).getSubject());
	  assertEquals("pancake@partychapp.appspotmail.com",
			  mailer.sentMessages.get(0).getSender());
  }

  public void testSummonUnknownAlias() {
    handler.doCommand(Message.createForTests("/summon fdsakfj"));
    assertEquals(2, xmpp.messages.size());
    assertEquals("[neil] /summon fdsakfj", xmpp.messages.get(0).getBody());
    assertEquals("Could not find member with input 'fdsakfj.'", xmpp.messages.get(1).getBody());
    
    assertEquals(0, mailer.sentMessages.size());
  }
  
  public void testDidYouMean1() {
    handler.doCommand(Message.createForTests("/summon jaso"));
    assertEquals(2, xmpp.messages.size());
    assertEquals("[neil] /summon jaso", xmpp.messages.get(0).getBody());
    assertEquals("Could not find member with input 'jaso.' Maybe you meant 'jason.'", xmpp.messages.get(1).getBody());
    
    assertEquals(0, mailer.sentMessages.size());
  }

  public void testFindByEmailAddressBeginning() {
    Channel c = FakeDatastore.fakeChannel();
    c.getMemberByAlias("jason").setAlias("intern");
    c.put();
    
    handler.doCommand(Message.createForTests("/summon jason"));
    assertEquals(2, xmpp.messages.size());
    assertEquals("[neil] /summon jason", xmpp.messages.get(0).getBody());
    assertEquals("_neil summoned intern_", xmpp.messages.get(1).getBody());
    
    assertEquals(1, mailer.sentMessages.size());
  }

  public void testFindByEmailAddress() {
    Channel c = FakeDatastore.fakeChannel();
    c.getMemberByAlias("jason").setAlias("intern");
    c.put();
    
    handler.doCommand(Message.createForTests("/summon jason@gmail.com"));
    assertEquals(2, xmpp.messages.size());
    assertEquals("[neil] /summon jason@gmail.com", xmpp.messages.get(0).getBody());
    assertEquals("_neil summoned intern_", xmpp.messages.get(1).getBody());
    
    assertEquals(1, mailer.sentMessages.size());
  }

  
  public void testException() {
    mailer.setThrowException();
    handler.doCommand(Message.createForTests("/summon jason"));
    assertEquals(2, xmpp.messages.size());
    assertEquals("[neil] /summon jason", xmpp.messages.get(0).getBody());
    assertEquals("Error while sending mail to 'jason@gmail.com'. Email may not have been sent.",
                 xmpp.messages.get(1).getBody());
  }
}
