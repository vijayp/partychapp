package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;

public class StatsHandlerTest extends CommandHandlerTest {
  StatsHandler handler = new StatsHandler();
  
  public void testStats() {
    handler.doCommand(Message.createForTests(" /stats"));
    assertEquals("Number of channels: 1\n" +
                 "Number of users: 5\n" +
                 "1-day active users: 5\n" + 
                 "1-day active users: 1\n" +
                 "7-day active users: 2\n" +
                 "30-day active users: 3\n" +
                 "Some stats were last refreshed at: Wed Oct 21 10:21:10 EDT 2009",
                 xmpp.messages.get(0).getBody());
  }
}
