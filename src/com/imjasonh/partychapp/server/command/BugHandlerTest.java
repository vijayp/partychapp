package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;

public class BugHandlerTest extends CommandHandlerTest {
  BugHandler handler = new BugHandler();

  public void testFileBug() {
    handler.doCommand(Message.createForTests("/bug I don't like this behavior"));
    
    assertEquals(1, xmpp.messages.size());
    assertEquals("http://code.google.com/p/partychapp/issues/entry?summary=I+don%27t+like+this+behavior&comment=Filed+by+user+neil+from+channel+pancake",
                 xmpp.messages.get(0).getBody());
  }
}
