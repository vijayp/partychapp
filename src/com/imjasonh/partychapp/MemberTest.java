package com.imjasonh.partychapp;

import junit.framework.TestCase;

public class MemberTest extends TestCase {
  public void setUp() {
	FakeDatastore datastore = new FakeDatastore();
	Datastore.setInstance(datastore);
	datastore.setUp();
  }
  
  public void testAddToLastMessages() {
    Member m = FakeDatastore.fakeChannel().getMemberByAlias("neil");
    for (Integer i = 0; i < 20; ++i) {
      m.addToLastMessages(i.toString());
    }
    assertEquals(10, m.getLastMessages().size());
    for (int i = 0; i < 10; ++i) {
      Integer val = 19 - i;
      assertEquals(val.toString(), m.getLastMessages().get(i));
    }
  }
}