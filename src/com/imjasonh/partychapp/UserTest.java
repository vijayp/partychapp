package com.imjasonh.partychapp;

import com.google.appengine.api.xmpp.JID;

import com.imjasonh.partychapp.testing.FakeDatastore;

import junit.framework.TestCase;

/**
 * Tests for the {@link User} class.
 * 
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class UserTest extends TestCase {
  private FakeDatastore datastore;
  private User userLowercase;
  private User userUppercase;
  private Channel channel1;
  private Channel channel2;
  
  @Override
  public void setUp() {
    datastore = new FakeDatastore();
    Datastore.setInstance(datastore);
    
    userLowercase = datastore.getOrCreateUser("user@gmail.com");
    userUppercase = datastore.getOrCreateUser("USER@gmail.com");
    datastore.put(userLowercase);
    datastore.put(userUppercase);    
    
    channel1 = new Channel(new JID("channel1@partychat"));
    channel2 = new Channel(new JID("channel2@partychat"));
    datastore.put(channel1);
    datastore.put(channel2);
  }
  
  public void testMergeNotAllowed() {
    // Should be allowed to merge users that differ only in capitalization 
    userLowercase.merge(userUppercase);
    
    // But not entirely different users
    User userOther = datastore.getOrCreateUser("user.other@gmail.com");
    datastore.put(userOther);
    
    try {
      userLowercase.merge(userOther);
      fail("Expected to not be allowed to merge");
    } catch (IllegalArgumentException err) {
      // Expected
    }
    
    // Or the same user
    try {
      userLowercase.merge(userLowercase);
      fail("Expected to not be allowed to merge");
    } catch (IllegalArgumentException err) {
      // Expected
    }    
  }
  
  public void testMergeChannels() {
    assertNotNull(datastore.getUserByJID("USER@gmail.com"));

    // Start out with the lower-case user in channel1 and the upper-case one in 
    // channel 2
    channel1.addMember(userLowercase);
    channel2.addMember(userUppercase);
    
    assertNotNull(channel1.getMemberByJID(userLowercase.getJID()));
    assertNotNull(channel2.getMemberByJID(userUppercase.getJID()));
    assertEquals(1, userLowercase.channelNames().size());
    assertTrue(userLowercase.channelNames().contains("channel1"));
    assertEquals(1, userUppercase.channelNames().size());
    assertTrue(userUppercase.channelNames().contains("channel2"));
    
    // Merge the upper-case one into the lower-case one
    userLowercase.merge(userUppercase);
    
    // The lower-case user should now be in both channels, and the upper-case
    // one should be in neither
    assertNotNull(channel1.getMemberByJID(userLowercase.getJID()));
    assertNotNull(channel2.getMemberByJID(userLowercase.getJID()));
    assertNull(channel1.getMemberByJID(userUppercase.getJID()));
    assertNull(channel2.getMemberByJID(userUppercase.getJID()));
    assertEquals(2, userLowercase.channelNames().size());
    assertTrue(userLowercase.channelNames().contains("channel1"));
    assertTrue(userLowercase.channelNames().contains("channel2"));    
    assertEquals(0, userUppercase.channelNames().size());
    
    // The upper-case user should also be gone from the datastore
    assertNull(datastore.getUserByJID("USER@gmail.com"));
  }    
  
  /**
   * Variant of {@link #testMergeChannels} where the lower-case user is already
   * in all of the channels that the upper-case one is, so there's no need to
   * modify it. 
   */
  public void testMergeChannelsNoOp() {
    assertNotNull(datastore.getUserByJID("USER@gmail.com"));

    channel1.addMember(userLowercase);
    channel2.addMember(userLowercase);
    channel2.addMember(userUppercase);
    
    assertNotNull(channel1.getMemberByJID(userLowercase.getJID()));
    assertNotNull(channel2.getMemberByJID(userLowercase.getJID()));
    assertNotNull(channel2.getMemberByJID(userUppercase.getJID()));
    assertEquals(2, userLowercase.channelNames().size());
    assertTrue(userLowercase.channelNames().contains("channel1"));
    assertTrue(userLowercase.channelNames().contains("channel2"));
    assertEquals(1, userUppercase.channelNames().size());
    assertTrue(userUppercase.channelNames().contains("channel2"));
    
    userLowercase.merge(userUppercase);
    
    assertNotNull(channel1.getMemberByJID(userLowercase.getJID()));
    assertNotNull(channel2.getMemberByJID(userLowercase.getJID()));
    assertNull(channel1.getMemberByJID(userUppercase.getJID()));
    assertNull(channel2.getMemberByJID(userUppercase.getJID()));
    assertEquals(2, userLowercase.channelNames().size());
    assertTrue(userLowercase.channelNames().contains("channel1"));
    assertTrue(userLowercase.channelNames().contains("channel2"));    
    assertEquals(0, userUppercase.channelNames().size());
    
    assertNull(datastore.getUserByJID("USER@gmail.com"));    
  }
  
  
}