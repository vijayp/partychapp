package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;

public class ReasonsHandlerTest extends CommandHandlerTestCase {
  ReasonsHandler handler = new ReasonsHandler();
  PPBHandler ppb = new PPBHandler();

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
    ppb.doCommand(Message.createForTests(content2));
    xmpp.messages.clear();

    handler.doCommand(Message.createForTests("/reasons x"));
    assertEquals(1, xmpp.messages.size());
    assertEquals("x: 0\n" +
                 "decrement by neil@gmail.com (" + content2 + ")\n" +
                 "increment by neil@gmail.com (" + content1 + ")\n" +
                 "More reasons may be visible at " +
                     "http://partychapp.appspot.com/channel/pancake",
                 xmpp.messages.get(0).getBody());
    
    xmpp.messages.clear();
    handler.doCommand(Message.createForTests("/reasons doesnotexit"));
    assertEquals(1, xmpp.messages.size());
    assertEquals("No reasons found", xmpp.messages.get(0).getBody());        
  }
}