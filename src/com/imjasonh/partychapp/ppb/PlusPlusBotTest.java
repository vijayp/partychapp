package com.imjasonh.partychapp.ppb;

import java.util.List;

import junit.framework.TestCase;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.FakeDatastore;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.ppb.PlusPlusBot.Action;

public class PlusPlusBotTest extends TestCase {
  private PlusPlusBot ppb = new PlusPlusBot();

  public void setUp() {
	FakeDatastore datastore = new FakeDatastore();
    Datastore.setInstance(datastore);
    datastore.setUp();
  }

  private void assertReasonEquals(
          String content,
          String name,
          int scoreAfter,
          PlusPlusBot.Action act,
          Reason actual) {
    assertEquals(name, actual.target().name());
    assertEquals(content, actual.reason());
    assertEquals(act, actual.action());
    assertEquals(scoreAfter, actual.scoreAfter());
  }

  private void assertScore(final String entity, final int score) {
    Channel fake_channel = FakeDatastore.fakeChannel();
    assertEquals(ppb.getScoreForEntity(fake_channel, entity), score);

  }
  private List<Reason> runAndGetReasons(String content) {
    Message m = Message.createForTests(content);
    assertTrue(ppb.matches(content));
    List<Reason> reasons = ppb.extractReasons(m);
    assertNotNull(reasons);
    return reasons;
  }

  private Reason runAndGetOneReason(String content) {
    List<Reason> reasons = runAndGetReasons(content);
    assertEquals(1, reasons.size());
    return reasons.get(0);
  }
 
  public void testPlusPlus() {
    String content = "mihai++ for knowing everything";
    Reason r = runAndGetOneReason(content);
    assertReasonEquals(content, "mihai", 1, Action.PLUSPLUS, r);
  }
  
  public void testMinusMinus() {
    String content = "dolapo-- for something random";
    Reason r = runAndGetOneReason(content);
    assertReasonEquals(content, "dolapo", -1, Action.MINUSMINUS, r);
  }

  public void testInMiddleOfMessage() {
    String content = "that was ridiculous. dolapo-- for something random";
    Reason r = runAndGetOneReason(content);
    assertReasonEquals(content, "dolapo", -1, Action.MINUSMINUS, r);    
    assertScore("dolapo", -1);
  }

  public void testCaseStability() {
    String content = "The habs-- lost again!";
    Reason r = runAndGetOneReason(content);
    assertReasonEquals(content, "habs", -1, Action.MINUSMINUS, r);
    
    // Verify that the scores are the same regardless of the case used
    assertScore("habs", -1);
    assertScore("Habs", -1);
    
    content = "OMG THE HABS++ WON!!";
    r = runAndGetOneReason(content);
    assertReasonEquals(content, "habs", 0, Action.PLUSPLUS, r);

    // Verify that the scores are the same regardless of the case used
    assertScore("habs", 0);
    assertScore("Habs", 0);
    assertScore("HABS", 0);

    }
 
  public void testAllCaps() {
	    String content = "The F-- train screwed me over again";
	    Reason r = runAndGetOneReason(content);
	    assertReasonEquals(content, "f", -1, Action.MINUSMINUS, r);    
	  }
  
  public void testMultiple() {
    String content = "kushal-- dbentley-- mihai++ rohit++";
    List<Reason> reasons = runAndGetReasons(content);
    assertEquals(4, reasons.size());
    assertReasonEquals(content, "kushal", -1, Action.MINUSMINUS, reasons.get(0));
    assertReasonEquals(content, "dbentley", -1, Action.MINUSMINUS, reasons.get(1));
    assertReasonEquals(content, "mihai", 1, Action.PLUSPLUS, reasons.get(2));
    assertReasonEquals(content, "rohit", 1, Action.PLUSPLUS, reasons.get(3));

    // Verify the score in the datastore, not just the reason list:
    assertScore("kushal", -1);
    assertScore("dbentley", -1);
    assertScore("mihai", 1);
    assertScore("rohit", 1);

  }

  public void testDoubleUp() {
    // this should trigger twice
    String content = "nsanch-- nsanch-- nsanch++ nsanch--";
    List<Reason> reasons = runAndGetReasons(content);
    assertEquals(4, reasons.size());    
    assertReasonEquals(content, "nsanch", -1, Action.MINUSMINUS, reasons.get(0));
    assertReasonEquals(content, "nsanch", -2, Action.MINUSMINUS, reasons.get(1));
    assertReasonEquals(content, "nsanch", -1, Action.PLUSPLUS, reasons.get(2));
    assertReasonEquals(content, "nsanch", -2, Action.MINUSMINUS, reasons.get(3));
    assertScore("nsanch", -2);
  }

  public void testTwoInARow() {
    String content1 = "nsanch--";
    Reason r1 = runAndGetOneReason(content1);
    assertReasonEquals(content1, "nsanch", -1, Action.MINUSMINUS, r1);
    
    String content2 = "nsanch++";
    Reason r2 = runAndGetOneReason(content2);
    assertReasonEquals(content2, "nsanch", 0, Action.PLUSPLUS, r2);
    assertScore("nsanch", 0);

  }
  
  public void testBlacklist() {
    String content = "i don't understand c++ but i love java++";
    Reason r = runAndGetOneReason(content);
    assertReasonEquals(content, "java", 1, Action.PLUSPLUS, r);
    assertScore("c", 0);
    assertScore("java", 1);
  }
  
  public void testBlacklist2() {
    String content = "<------- <-- <--- c++";
    List<Reason> reasons = runAndGetReasons(content);
    assertEquals(0, reasons.size());
  }

  public void testWithPunctuation() {
    String content = "_hello-world.foo+++";
    Reason r = runAndGetOneReason(content);
    assertReasonEquals(content, "_hello-world.foo+", 1, Action.PLUSPLUS, r);
  }
  
  public void testCPP() {
    String content1 = "c++++";
    Reason r1 = runAndGetOneReason(content1);
    assertReasonEquals(content1, "c++", 1, Action.PLUSPLUS, r1);
    assertScore("c++", 1);

    String content2 = "c++--";
    Reason r2 = runAndGetOneReason(content2);
    assertReasonEquals(content2, "c++", 0, Action.MINUSMINUS, r2);
    assertScore("c++", 0);

  }
  
  public void testNoMatch() {
    assertFalse(ppb.matches("x++y"));
  }
}