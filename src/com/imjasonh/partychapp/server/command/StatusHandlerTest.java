package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.User;
import com.imjasonh.partychapp.testing.FakeDatastore;

public class StatusHandlerTest extends CommandHandlerTest {
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
    User u = FakeDatastore.fakeChannel().getMemberByAlias("neil").user();
    u.setPhoneNumber("16464623000");
    u.setCarrier(User.Carrier.TMOBILE);

    handler.doCommand(Message.createForTests("/status"));
    assertEquals(1, xmpp.messages.size());
    assertEquals("You are currently in 'pancake' as 'neil.' Your phone number " +
                 "is 16464623000. Your carrier is tmobile.",
                 xmpp.messages.get(0).getBody());    
  }
}