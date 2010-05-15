package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.Message.MessageType;

public class ReasonsHandlerTest extends CommandHandlerTestCase {
  ReasonsHandler handler = new ReasonsHandler();
  PPBHandler ppb = new PPBHandler();
  LeaveHandler leave = new LeaveHandler();

  public void testMatches() {
    assertTrue(handler.matches(Message.createForTests("/reasons x")));
    assertTrue(handler.matches(Message.createForTests(" /reasons x")));
    assertTrue(handler.matches(Message.createForTests("/reasons xyz")));
    assertTrue(handler.matches(Message.createForTests("/reasons a_b-c.d")));
    assertFalse(handler.matches(Message.createForTests("x /reasons")));
  }
  
  public void testReasons() {
    String content1 = "x++ for being awesome";
    ppb.doCommand(Message.createForTests(content1));
    xmpp.messages.clear();

    String content2 = "x-- is so lame";
    ppb.doCommand(
        Message.createForTests(content2, MessageType.XMPP, "david@gmail.com"));
    xmpp.messages.clear();

    handler.doCommand(Message.createForTests("/reasons x"));
    assertEquals(1, xmpp.messages.size());
    assertEquals("x: 0\n" +
                 "decrement by david (" + content2 + ")\n" +
                 "increment by neil (" + content1 + ")",
                 xmpp.messages.get(0).getBody());

    
    xmpp.messages.clear();
    handler.doCommand(Message.createForTests("/reasons doesnotexit"));
    assertEquals(1, xmpp.messages.size());
    assertEquals("No reasons found", xmpp.messages.get(0).getBody());

    // Output should still work even if the person that did the incrementing/
    // decrementing is no longer in the channel.
    leave.doCommand(
        Message.createForTests("/leave", MessageType.XMPP, "david@gmail.com"));
    xmpp.messages.clear();
    handler.doCommand(Message.createForTests("/reasons x"));
    assertEquals(1, xmpp.messages.size());
    assertTrue(xmpp.messages.get(0).getBody().startsWith("x: 0"));
  }
}