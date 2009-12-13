package com.imjasonh.partychapp.server.command;

import junit.framework.TestCase;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.FakeDatastore;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.MockXMPPService;
import com.imjasonh.partychapp.server.SendUtil;

public class StatsHandlerTest extends CommandHandlerTest {
  StatsHandler handler = new StatsHandler();
  
  public void testStats() {
    handler.doCommand(Message.createForTests(" /stats"));
    assertEquals("Number of channels: 1\nNumber of users (it's actually more than this): 5\nStats last refreshed: Wed Oct 21 10:21:10 EDT 2009", xmpp.messages.get(0).getBody());
  }
}
