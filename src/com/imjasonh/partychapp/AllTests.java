package com.imjasonh.partychapp;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.imjasonh.partychapp.ppb.PlusPlusBotTest;
import com.imjasonh.partychapp.server.PartychappServletTest;
import com.imjasonh.partychapp.server.command.AliasHandlerTest;
import com.imjasonh.partychapp.server.command.BroadcastHandlerTest;
import com.imjasonh.partychapp.server.command.DebugHandlerTest;
import com.imjasonh.partychapp.server.command.EmailHandlerTest;
import com.imjasonh.partychapp.server.command.GraphScoreHandlerTest;
import com.imjasonh.partychapp.server.command.InviteHandlerTest;
import com.imjasonh.partychapp.server.command.LeaveHandlerTest;
import com.imjasonh.partychapp.server.command.ListHandlerTest;
import com.imjasonh.partychapp.server.command.MeHandlerTest;
import com.imjasonh.partychapp.server.command.PPBHandlerTest;
import com.imjasonh.partychapp.server.command.ReasonsHandlerTest;
import com.imjasonh.partychapp.server.command.SMSHandlerTest;
import com.imjasonh.partychapp.server.command.ScoreHandlerTest;
import com.imjasonh.partychapp.server.command.SearchReplaceHandlerTest;
import com.imjasonh.partychapp.server.command.SnoozeHandlerTest;
import com.imjasonh.partychapp.server.command.StatsHandlerTest;
import com.imjasonh.partychapp.server.command.StatusHandlerTest;
import com.imjasonh.partychapp.server.command.SummonHandlerTest;
import com.imjasonh.partychapp.server.command.UndoHandlerTest;

public class AllTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("Test for com.imjasonh.partychapp");
    // $JUnit-BEGIN$
    suite.addTestSuite(PlusPlusBotTest.class);
    suite.addTestSuite(AliasHandlerTest.class);
    suite.addTestSuite(ListHandlerTest.class);
    suite.addTestSuite(PPBHandlerTest.class);
    suite.addTestSuite(LeaveHandlerTest.class);
    suite.addTestSuite(ReasonsHandlerTest.class);
    suite.addTestSuite(ScoreHandlerTest.class);
    suite.addTestSuite(MeHandlerTest.class);
    suite.addTestSuite(SearchReplaceHandlerTest.class);
    suite.addTestSuite(PartychappServletTest.class);
    suite.addTestSuite(MemberTest.class);
    suite.addTestSuite(StatusHandlerTest.class);
    suite.addTestSuite(BroadcastHandlerTest.class);
    suite.addTestSuite(SummonHandlerTest.class);
    suite.addTestSuite(UndoHandlerTest.class);
    suite.addTestSuite(DebugHandlerTest.class);
    suite.addTestSuite(InviteHandlerTest.class);
    suite.addTestSuite(StatsHandlerTest.class);
    suite.addTestSuite(GraphScoreHandlerTest.class);
    suite.addTestSuite(SnoozeHandlerTest.class);
    suite.addTestSuite(EmailHandlerTest.class);
    suite.addTestSuite(SMSHandlerTest.class);
    // $JUnit-END$
    return suite;
  }

}
