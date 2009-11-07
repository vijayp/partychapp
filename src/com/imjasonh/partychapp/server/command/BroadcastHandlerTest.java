package com.imjasonh.partychapp.server.command;

import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import com.google.appengine.api.xmpp.JID;
import com.google.appengine.repackaged.com.google.common.collect.Lists;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.FakeDatastore;
import com.imjasonh.partychapp.Member;
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
        FakeDatastore.fakeChannel().getMemberByAlias("neil").getLastMessages();
    assertEquals(1, lastMessages.size());
    assertEquals("test", lastMessages.get(0));
  }
  
  public void testSnoozersDontGetMessages() {
    Member jason = null;
    jason = FakeDatastore.fakeChannel().getMemberByAlias("jason");
    // set jason to snooze for a minute
    jason.setSnoozeUntil(new Date(System.currentTimeMillis() + 1000*60));
    jason.put();
    
    handler.doCommand(Message.createForTests("test"));
    
    assertEquals(1, xmpp.messages.size());
    List<String> testJids = Lists.newArrayList();
    for (JID j : xmpp.messages.get(0).getRecipientJids()) {
      testJids.add(j.getId());
    }
    System.out.println(testJids);
    assertFalse(testJids.contains("jason@gmail.com"));
    
    xmpp.messages.clear();
    // wake him up
    jason = FakeDatastore.fakeChannel().getMemberByAlias("jason");
    jason.setSnoozeUntil(new Date(System.currentTimeMillis() - 100));
    jason.put();
    handler.doCommand(Message.createForTests("test 2"));

    //assertEquals(2, xmpp.messages.size());
    assertEquals("[neil] test 2", xmpp.messages.get(0).getBody());
    assertEquals(null,
                 FakeDatastore.fakeChannel().getMemberByAlias("jason").getSnoozeUntil());

    assertEquals("_jason is no longer snoozing_", xmpp.messages.get(1).getBody());
    List<String> jids = Lists.newArrayList();
    for (JID j : xmpp.messages.get(0).getRecipientJids()) {
      jids.add(j.getId());
    }
    assertTrue(jids.contains("jason@gmail.com"));
  }
}