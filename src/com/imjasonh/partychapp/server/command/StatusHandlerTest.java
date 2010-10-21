package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.DebuggingOptions;
import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.User;
import com.imjasonh.partychapp.testing.FakeDatastore;

public class StatusHandlerTest extends CommandHandlerTestCase {
  StatusHandler handler = new StatusHandler();

  public void testMatches() {
    assertTrue(handler.matches(Message.createForTests("/status")));
    assertTrue(handler.matches(Message.createForTests(" /status")));
  }
  
  public void testInRoom() {
    handler.doCommand(Message.createForTests("/status"));
    assertEquals(1, xmpp.messages.size());
    assertEquals("You are currently in 'pancake' as 'neil.'", xmpp.messages.get(0).getBody());
  }
  
  public void testWithPhoneNumberAndCarrier() {
    Message message = Message.createForTests("/status");
    User u = message.user;
    u.setPhoneNumber("16464623000");
    u.setCarrier(User.Carrier.TMOBILE);

    handler.doCommand(message);
    assertEquals(1, xmpp.messages.size());
    assertEquals("You are currently in 'pancake' as 'neil.' Your phone number " +
                 "is 16464623000. Your carrier is tmobile.",
                 xmpp.messages.get(0).getBody());    
  }
  
  public void testSequenceId() {
    Member member = FakeDatastore.fakeChannel().getMemberByAlias("neil");
    member.debugOptions().add(DebuggingOptions.Option.SEQUENCE_IDS);
    handler.doCommand(Message.createForTests("/status"));
    assertEquals(1, xmpp.messages.size());
    assertEquals("You are currently in 'pancake' as 'neil.'\n" +
        "Current sequence ID: 0", xmpp.messages.get(0).getBody());
    member.debugOptions().clear();
  }  
}