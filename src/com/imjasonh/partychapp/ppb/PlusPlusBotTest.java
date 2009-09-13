package com.imjasonh.partychapp.ppb;

import java.util.List;

import junit.framework.TestCase;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.FakeDatastore;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.ppb.PlusPlusBot.Action;

public class PlusPlusBotTest extends TestCase {
  private PlusPlusBot ppb = new PlusPlusBot();

  public void setUp() {
    Datastore.setInstance(new FakeDatastore());
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
  }
  
  public void testMultiple() {
    String content = "kushal-- dbentley-- mihai++ rohit++";
    List<Reason> reasons = runAndGetReasons(content);
    assertEquals(4, reasons.size());
    assertReasonEquals(content, "kushal", -1, Action.MINUSMINUS, reasons.get(0));
    assertReasonEquals(content, "dbentley", -1, Action.MINUSMINUS, reasons.get(1));
    assertReasonEquals(content, "mihai", 1, Action.PLUSPLUS, reasons.get(2));
    assertReasonEquals(content, "rohit", 1, Action.PLUSPLUS, reasons.get(3));
  }

  public void testNoDoubleUp() {
    // this should only trigger once
    String content = "nsanch-- nsanch--";
    Reason r = runAndGetOneReason(content);
    assertReasonEquals(content, "nsanch", -1, Action.MINUSMINUS, r);
  }

  public void testDoubleUpWithOverride() {
    // this should trigger twice
    String content = "nsanch-- nsanch-- /combine";
    List<Reason> reasons = runAndGetReasons(content);
    assertEquals(2, reasons.size());    
    assertReasonEquals(content, "nsanch", -1, Action.MINUSMINUS, reasons.get(0));
    assertReasonEquals(content, "nsanch", -2, Action.MINUSMINUS, reasons.get(1));
  }

  public void testTwoInARow() {
    String content1 = "nsanch--";
    Reason r1 = runAndGetOneReason(content1);
    assertReasonEquals(content1, "nsanch", -1, Action.MINUSMINUS, r1);
    
    String content2 = "nsanch++";
    Reason r2 = runAndGetOneReason(content2);
    assertReasonEquals(content2, "nsanch", 0, Action.PLUSPLUS, r2);
  }
  
  public void testBlacklist() {
    String content = "i don't understand c++ but i love java++";
    Reason r = runAndGetOneReason(content);
    assertReasonEquals(content, "java", 1, Action.PLUSPLUS, r);
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
 
    String content2 = "c++--";
    Reason r2 = runAndGetOneReason(content2);
    assertReasonEquals(content2, "c++", 0, Action.MINUSMINUS, r2);

  }
  
  public void testNoMatch() {
    assertFalse(ppb.matches("x++y"));
  }
}