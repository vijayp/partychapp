package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;

public class StatsHandlerTest extends CommandHandlerTest {
  StatsHandler handler = new StatsHandler();
  
  public void testStats() {
    handler.doCommand(Message.createForTests(" /stats"));
    assertEquals("Number of channels (as of 10/21/09 10:21 AM): 1\n" +
                 "1-day active users: 1\n" +
                 "7-day active users: 2\n" +
                 "Number of users: 5\n",
                 xmpp.messages.get(0).getBody());
  }
}
