package com.imjasonh.partychapp;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.imjasonh.partychapp.ppb.PlusPlusBotTest;
import com.imjasonh.partychapp.server.command.AliasTest;
import com.imjasonh.partychapp.server.command.ListHandlerTest;
import com.imjasonh.partychapp.server.command.MeHandlerTest;
import com.imjasonh.partychapp.server.command.PPBHandlerTest;
import com.imjasonh.partychapp.server.command.ReasonsHandlerTest;
import com.imjasonh.partychapp.server.command.ScoreHandlerTest;

public class AllTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("Test for com.imjasonh.partychapp");
    // $JUnit-BEGIN$
    suite.addTestSuite(PlusPlusBotTest.class);
    suite.addTestSuite(AliasTest.class);
    suite.addTestSuite(ListHandlerTest.class);
    suite.addTestSuite(PPBHandlerTest.class);
    suite.addTestSuite(ReasonsHandlerTest.class);
    suite.addTestSuite(ScoreHandlerTest.class);
    suite.addTestSuite(MeHandlerTest.class);
    // $JUnit-END$
    return suite;
  }

}
