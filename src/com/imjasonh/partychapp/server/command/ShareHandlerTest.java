package com.imjasonh.partychapp.server.command;

import com.google.common.collect.ImmutableMap;

import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.urlinfo.MockUrlInfoService;
import com.imjasonh.partychapp.urlinfo.UrlInfo;

import java.net.URI;

/**
 * Tests for {@link ShareHandler}
 * 
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class ShareHandlerTest extends CommandHandlerTestCase {
  private static final ShareHandler handler = new ShareHandler(
      new MockUrlInfoService(
          ImmutableMap.of(
              URI.create("http://example.com"), new UrlInfo("title", ""))));
  
  public void testMatches() {
    assertTrue(handler.matches(Message.createForTests(
        "/share")));
    assertTrue(handler.matches(Message.createForTests(
        "/share http://example.com")));
    assertTrue(handler.matches(Message.createForTests(
        "/share http://example.com this site rocks")));
  }
  
  public void testCommand() {
    // Basic announcement
    handler.doCommand(Message.createForTests("/share http://example.com"));
    assertEquals(1, xmpp.messages.size());
    assertEquals(
        "_neil is sharing http://example.com (title)_",
        xmpp.messages.get(0).getBody());    

    // Announcement with annotation
    xmpp.messages.clear();
    handler.doCommand(Message.createForTests(
        "/share http://example.com this site is awesome"));
    assertEquals(1, xmpp.messages.size());
    assertEquals(
        "_neil is sharing http://example.com (title) : this site is awesome_",
        xmpp.messages.get(0).getBody());    
  }
}