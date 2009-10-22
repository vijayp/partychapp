package com.imjasonh.partychapp.server.command;

import java.text.DateFormat;
import java.util.Date;

import junit.framework.TestCase;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.FakeDatastore;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.MockXMPPService;
import com.imjasonh.partychapp.server.SendUtil;

public class SnoozeHandlerTest extends TestCase {
  SnoozeHandler handler = new SnoozeHandler();
  MockXMPPService xmpp = new MockXMPPService();
  
  public void setUp() {
    Datastore.setInstance(new FakeDatastore());
    SendUtil.setXMPP(xmpp);
    // 10/21/2009 at 10:21:10 am EDT
    handler.setTimeForTesting(1256134870830L);
  }
  
  public void testMatches() {
    assertTrue(handler.matches(Message.createForTests("/snooze 20m")));
    assertTrue(handler.matches(Message.createForTests(" /snooze 392s")));
  }
  
  public void snoozeAndGetDate(String cmd, String reply, String date) {
    handler.doCommand(Message.createForTests(cmd));
    assertEquals(1, xmpp.messages.size());
    assertEquals(reply + ", until " + date, xmpp.messages.get(0).getBody());
    Date actual = FakeDatastore.instance().fakeChannel().getMemberByAlias("neil").getSnoozeUntil();
    assertEquals(date,
                 DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(actual));
  }

  public void testSimple1() {
    snoozeAndGetDate("/snooze 60s",
                     "Okay, snoozing for 60 seconds (60 seconds)",
                     "October 21, 2009 10:22:10 AM EDT");
  }

  public void testSimple2() {
    snoozeAndGetDate("/snooze 45m",
                     "Okay, snoozing for 45 minutes (2700 seconds)",
                     "October 21, 2009 11:06:10 AM EDT");
  }

  public void testSimple3() {
    snoozeAndGetDate("/snooze 3h",
                     "Okay, snoozing for 3 hours (10800 seconds)",
                     "October 21, 2009 1:21:10 PM EDT");
  }

  public void testSimple4() {
    // ooh, a bonus daylight savings test
    snoozeAndGetDate("/snooze 7d",
                     "Okay, snoozing for 7 days (604800 seconds)",
                     "October 28, 2009 9:21:10 AM EST");
  }
}