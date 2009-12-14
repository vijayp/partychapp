package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;

public class GraphScoreHandlerTest extends CommandHandlerTest {
  PPBHandler ppb = new PPBHandler();
  GraphScoreHandler handler = new GraphScoreHandler();
  
  public void testSimple() {
    String[] messages = { "x++", "x--", "x++", "x++", "x++", "x++", "x--", "x--", "x--", "x--", "x--", "x--", "x--",
           "y++", "y--", "y--", "y--", "y--", "y++", "y++", "y++", "y++", "y++", "y++", };
    for (String m : messages) {
      ppb.doCommand(Message.createForTests(m));
    }
    xmpp.messages.clear();

    handler.doCommand(Message.createForTests("/graph-score x y"));
    String url = xmpp.messages.get(0).getBody();
    // I don't really care about the URL contents.
    assertTrue(url.startsWith("http://"));
  }
}