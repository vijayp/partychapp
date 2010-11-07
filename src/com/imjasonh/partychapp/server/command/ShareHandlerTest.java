package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;

import java.net.URI;

/**
 * Tests for {@link ShareHandler}
 * 
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class ShareHandlerTest extends CommandHandlerTestCase {
  /**
   * Testable version of {@link ShareHandler} that lets us fake out the URL
   * fetching.
   */
  private static class TestShareHandler extends ShareHandler {
    private String uriContents = null;
    
    @Override protected String getUriContents(URI uri) {
      return uriContents;
    }
    
    public void setUriContents(String uriContents) {
      this.uriContents = uriContents;
    }
  }
  
  TestShareHandler handler = new TestShareHandler();
  
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
    handler.setUriContents("foo<title>title</title>bar");
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

    // No title
    xmpp.messages.clear();
    handler.setUriContents("adasdasd");
    handler.doCommand(Message.createForTests("/share http://example.com"));
    assertEquals(1, xmpp.messages.size());
    assertEquals(
        "_neil is sharing http://example.com_",
        xmpp.messages.get(0).getBody());    

    // No contents fetched
    xmpp.messages.clear();
    handler.setUriContents(null);
    handler.doCommand(Message.createForTests("/share http://example.com"));
    assertEquals(1, xmpp.messages.size());
    assertEquals(
        "_neil is sharing http://example.com_",
        xmpp.messages.get(0).getBody());    
    
    // Title with whitespace in it
    xmpp.messages.clear();
    handler.setUriContents("foo<title>title   foo\n\n  bar   </title>bar");
    handler.doCommand(Message.createForTests("/share http://example.com"));
    assertEquals(1, xmpp.messages.size());
    assertEquals(
        "_neil is sharing http://example.com (title foo bar)_",
        xmpp.messages.get(0).getBody());        

    // Title with attributes
    xmpp.messages.clear();
    handler.setUriContents("foo<title id=\"the-title\">title</title>bar");
    handler.doCommand(Message.createForTests("/share http://example.com"));
    assertEquals(1, xmpp.messages.size());
    assertEquals(
        "_neil is sharing http://example.com (title)_",
        xmpp.messages.get(0).getBody());        

    // Title with escaped HTML
    xmpp.messages.clear();
    handler.setUriContents("<title>Python quiz &laquo; Vijay Pandurangan&#039;s blog</title>");
    handler.doCommand(Message.createForTests("/share http://example.com"));
    assertEquals(1, xmpp.messages.size());
    assertEquals(
        "_neil is sharing http://example.com (Python quiz \u00AB Vijay Pandurangan's blog)_",
        xmpp.messages.get(0).getBody());
  }
}