package com.imjasonh.partychapp.server.command;

import java.util.List;

import junit.framework.TestCase;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.FakeDatastore;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.MockXMPPService;
import com.imjasonh.partychapp.server.SendUtil;

public class BroadcastHandlerTest extends TestCase {
  BroadcastHandler handler = new BroadcastHandler();
  MockXMPPService xmpp = new MockXMPPService();
  
  public void setUp() {
    Datastore.setInstance(new FakeDatastore());
    SendUtil.setXMPP(xmpp);
  }

  
  public void testAddToLastMessages() {
    handler.doCommand(Message.createForTests("test"));
    
    List<String> lastMessages =
        FakeDatastore.instance().fakeChannel().getMemberByAlias("neil").getLastMessages();
    assertEquals(1, lastMessages.size());
    assertEquals("test", lastMessages.get(0));
  }
}
