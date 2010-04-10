package com.imjasonh.partychapp.server.command;

import java.text.DateFormat;
import java.util.Date;

import com.imjasonh.partychapp.FakeDatastore;
import com.imjasonh.partychapp.Message;

public class SnoozeHandlerTest extends CommandHandlerTest {
  SnoozeHandler handler = new SnoozeHandler();
  
  @Override
  public void setUp() {
    super.setUp();
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
    Date actual = FakeDatastore.fakeChannel().getMemberByAlias("neil").getSnoozeUntil();
    assertNotNull(actual);
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
    snoozeAndGetDate("/snooze 4d",
                     "Okay, snoozing for 4 days (345600 seconds)",
                     "October 25, 2009 9:21:10 AM EST");
  }
}