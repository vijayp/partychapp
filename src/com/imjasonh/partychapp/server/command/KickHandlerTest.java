package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.testing.FakeDatastore;

/**
 * Tests for {@link KickHandler}
 * 
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class KickHandlerTest extends CommandHandlerTestCase {
  KickHandler handler = new KickHandler();
  
  public void testKickByAlias() {
    handler.doCommand(Message.createForTests("/kick jason"));
    assertEquals(1, xmpp.messages.size());
    assertEquals("_neil kicked jason_", xmpp.messages.get(0).getBody());
    assertNull(FakeDatastore.fakeChannel().getMemberByJID("jason@gmail.com"));
  }
  
  public void testKickByJid() {
    handler.doCommand(Message.createForTests("/kick jason@gmail.com"));
    assertEquals(1, xmpp.messages.size());
    assertEquals(
        "_neil kicked jason@gmail.com_", xmpp.messages.get(0).getBody());
    assertNull(FakeDatastore.fakeChannel().getMemberByJID("jason@gmail.com"));
  }  

  public void testKickNoArgs() {
    handler.doCommand(Message.createForTests("/kick"));
    assertEquals(1, xmpp.messages.size());
    assertEquals(
        "You must specify someone to kick", xmpp.messages.get(0).getBody());
  }  
  
  public void testKickNoMember() {
    handler.doCommand(Message.createForTests("/kick noone"));
    assertEquals(1, xmpp.messages.size());
    assertEquals("No such member", xmpp.messages.get(0).getBody());
  }    
  
  public void testKickInvitee() {
    FakeDatastore.fakeChannel().invite("mihai@gmail.com");
    handler.doCommand(Message.createForTests("/kick mihai@gmail.com"));
    assertEquals(1, xmpp.messages.size());
    assertEquals(
        "_neil kicked mihai@gmail.com_", xmpp.messages.get(0).getBody()); 
    assertFalse(
        FakeDatastore.fakeChannel().getInvitees().contains("mihai@gmail.com"));
  }
  
  public void testKickRequestedInvitation() {
    FakeDatastore.fakeChannel().addRequestedInvitation("mihai@gmail.com");
    handler.doCommand(Message.createForTests("/kick mihai@gmail.com"));
    assertEquals(1, xmpp.messages.size());
    assertEquals(
        "_neil kicked mihai@gmail.com_", xmpp.messages.get(0).getBody());    
    assertFalse(
        FakeDatastore.fakeChannel().hasRequestedInvitation("mihai@gmail.com"));
  }  
}