package com.imjasonh.partychapp;

import junit.framework.TestCase;

import com.google.appengine.api.xmpp.JID;

public class MemberTest extends TestCase {
  public void testAddToLastMessages() {
    Member m = new Member(new JID("neil@gmail.com"));
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