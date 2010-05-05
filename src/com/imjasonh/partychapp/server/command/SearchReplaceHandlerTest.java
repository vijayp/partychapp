package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.testing.FakeDatastore;

public class SearchReplaceHandlerTest extends CommandHandlerTest {
  SearchReplaceHandler handler = new SearchReplaceHandler();
  BroadcastHandler bcast = new BroadcastHandler();
  PPBHandler ppbHandler = new PPBHandler();
  
  public void testMatches() {
    assertTrue(handler.matches(Message.createForTests("s/foo/bar/")));
    assertTrue(handler.matches(Message.createForTests("s/foo/bar/g")));
    assertTrue(handler.matches(Message.createForTests(" s/foo/bar/")));
    assertTrue(handler.matches(Message.createForTests("s/foo/bar")));
    assertTrue(handler.matches(Message.createForTests(" s/foo/bar")));
  }
  
  public void testSimple() {
    bcast.doCommand(Message.createForTests("foo foo"));
    xmpp.messages.clear();
    handler.doCommand(Message.createForTests("s/foo/bar/"));
    assertEquals(2, xmpp.messages.size());
    assertEquals("[neil] s/foo/bar/", xmpp.messages.get(0).getBody());
    assertEquals("_neil meant bar foo_", xmpp.messages.get(1).getBody());
  }
  
  public void testDeleteCaptured() {
    bcast.doCommand(Message.createForTests("foo foo"));
    xmpp.messages.clear();
    handler.doCommand(Message.createForTests("s/foo//"));
    assertEquals(2, xmpp.messages.size());
    assertEquals("[neil] s/foo//", xmpp.messages.get(0).getBody());
    assertEquals("_neil meant  foo_", xmpp.messages.get(1).getBody());    
  }
  
  public void testDeleteCapturedNoTrailingSlash() {
    bcast.doCommand(Message.createForTests("foo foo"));
    xmpp.messages.clear();
    handler.doCommand(Message.createForTests("s/foo/"));
    assertEquals(2, xmpp.messages.size());
    assertEquals("[neil] s/foo/", xmpp.messages.get(0).getBody());
    assertEquals("_neil meant  foo_", xmpp.messages.get(1).getBody());        
  }
  
  public void testDeleteCapturedGreedy() {
    bcast.doCommand(Message.createForTests("foo bar foo"));
    xmpp.messages.clear();
    handler.doCommand(Message.createForTests("s/foo//g"));
    assertEquals(2, xmpp.messages.size());
    assertEquals("[neil] s/foo//g", xmpp.messages.get(0).getBody());
    assertEquals("_neil meant  bar _", xmpp.messages.get(1).getBody());    
  }  
  public void testMissingTrailingSlash() {
    bcast.doCommand(Message.createForTests("foo foo"));
    xmpp.messages.clear();
    handler.doCommand(Message.createForTests("s/foo/bag"));
    assertEquals(2, xmpp.messages.size());
    assertEquals("[neil] s/foo/bag", xmpp.messages.get(0).getBody());
    assertEquals("_neil meant bag foo_", xmpp.messages.get(1).getBody());
  }
  
  public void testGreedy() {
    bcast.doCommand(Message.createForTests("foo bar baz foo bar baz"));
    xmpp.messages.clear();
    handler.doCommand(Message.createForTests("s/foo/bar/g"));
    assertEquals(2, xmpp.messages.size());
    assertEquals("[neil] s/foo/bar/g", xmpp.messages.get(0).getBody());
    assertEquals("_neil meant bar bar baz bar bar baz_", xmpp.messages.get(1).getBody());
  }
  
  public void testNoPlusPlusesChanged() {
    ppbHandler.doCommand(Message.createForTests("x++ foo"));
    xmpp.messages.clear();
    
    handler.doCommand(Message.createForTests("s/foo/bar/"));
    assertEquals(3, xmpp.messages.size());
    assertEquals("[neil] s/foo/bar/", xmpp.messages.get(0).getBody());
    assertEquals("Undoing original actions: x++ [back to 0]", xmpp.messages.get(1).getBody());
    assertEquals("_neil meant x++ [woot! now at 1] bar_", xmpp.messages.get(2).getBody());
  }

  public void testOnePlusPlusChanged() {
    ppbHandler.doCommand(Message.createForTests("x++ foo"));
    xmpp.messages.clear();

    handler.doCommand(Message.createForTests("s/x/y/"));
    assertEquals(3, xmpp.messages.size());
    assertEquals("[neil] s/x/y/", xmpp.messages.get(0).getBody());
    assertEquals("Undoing original actions: x++ [back to 0]", xmpp.messages.get(1).getBody());
    assertEquals("_neil meant y++ [woot! now at 1] foo_", xmpp.messages.get(2).getBody());
  }
  
  public void testManyChanges() {
    String firstMessage = "i am watching psych++ right now. sean-- for being an ass sometimes";
    ppbHandler.doCommand(Message.createForTests(firstMessage));
    xmpp.messages.clear();

    handler.doCommand(Message.createForTests("s/s/t/g"));
    assertEquals(3, xmpp.messages.size());
    assertEquals("[neil] s/s/t/g", xmpp.messages.get(0).getBody());
    assertEquals("Undoing original actions: psych++ [back to 0], sean-- [back to 0]", xmpp.messages.get(1).getBody());
    assertEquals("_neil meant i am watching ptych++ [woot! now at 1] right now. " +
                 "tean-- [ouch! now at -1] for being an att tometimet_",
                 xmpp.messages.get(2).getBody());    
  }
  
  public void testFixCombine() {
    // To start with, 'jason' is at 1 and 'intern' is at 2. We want to end with 'jason at 3 and 'intern' at 0.
    ppbHandler.doCommand(Message.createForTests("intern++"));
    ppbHandler.doCommand(Message.createForTests("intern++"));
    ppbHandler.doCommand(Message.createForTests("jason++"));
    
    // Oh no, a typo.
    String firstMessage = "jason++ jason++ intren-- intren-- /combine";
    ppbHandler.doCommand(Message.createForTests(firstMessage));
    xmpp.messages.clear();

    // fix the typo!
    handler.doCommand(Message.createForTests("s/intren/intern/g"));
    assertEquals(3, xmpp.messages.size());
    assertEquals("[neil] s/intren/intern/g", xmpp.messages.get(0).getBody());
    assertEquals("Undoing original actions: jason++ [back to 2], jason++ [back to 1], intren-- [back to -1], intren-- [back to 0]",
                 xmpp.messages.get(1).getBody());
    assertEquals("_neil meant jason++ [woot! now at 2] jason++ [woot! now at 3] intern-- [ouch! now at 1] intern-- [ouch! now at 0] /combine_",
                 xmpp.messages.get(2).getBody());    

  }
  
  public void testReplacePlusPlusWithMinusMinusThenOpposite() {
    String firstMessage = "x++";
    ppbHandler.doCommand(Message.createForTests(firstMessage));
    xmpp.messages.clear();

    handler.doCommand(Message.createForTests("s/\\+\\+/--/"));
    handler.doCommand(Message.createForTests("s/--/++/"));
    assertEquals(6, xmpp.messages.size());
    assertEquals("[neil] s/\\+\\+/--/", xmpp.messages.get(0).getBody());
    assertEquals("Undoing original actions: x++ [back to 0]", xmpp.messages.get(1).getBody());
    assertEquals("_neil meant x-- [ouch! now at -1]_",
                 xmpp.messages.get(2).getBody());
    assertEquals("[neil] s/--/++/", xmpp.messages.get(3).getBody());
    assertEquals("Undoing original actions: x-- [back to 0]", xmpp.messages.get(4).getBody());
    assertEquals("_neil meant x++ [woot! now at 1]_",
                 xmpp.messages.get(5).getBody());        
  }
  
  public void testRegexReplacement() {
    String firstMessage = "hello, world";
    ppbHandler.doCommand(Message.createForTests(firstMessage));
    xmpp.messages.clear();

    handler.doCommand(Message.createForTests("s/h.*,/$0 goodbye/"));
    assertEquals(2, xmpp.messages.size());
    assertEquals("[neil] s/h.*,/$0 goodbye/", xmpp.messages.get(0).getBody());
    assertEquals("_neil meant hello, goodbye world_",
                 xmpp.messages.get(1).getBody());
  }
  
  public void testSuggestReplacementForSelfIgnoresAlias() {
    ppbHandler.doCommand(Message.createForTests("hlleo world"));
    xmpp.messages.clear();

    handler.doCommand(Message.createForTests("neil: s/lle/ell/"));
    assertEquals(2, xmpp.messages.size());
    assertEquals("[neil] neil: s/lle/ell/", xmpp.messages.get(0).getBody());
    assertEquals("_neil meant hello world_",
                 xmpp.messages.get(1).getBody());    
  }
  
  public void testSuggestReplacementForOthers() {
    Channel c = FakeDatastore.fakeChannel();
    c.getMemberByAlias("jason").addToLastMessages("hlleo world");
    c.put();

    handler.doCommand(Message.createForTests("jason: s/lle/ell/"));
    assertEquals(2, xmpp.messages.size());
    assertEquals("[neil] jason: s/lle/ell/", xmpp.messages.get(0).getBody());
    assertEquals("_neil thinks jason meant hello world_",
                 xmpp.messages.get(1).getBody());    
  }
  
  public void testSuggestReplacementForOthersDoesntAffectPlusPluses() {
    Channel c = FakeDatastore.fakeChannel();
    c.getMemberByAlias("jason").addToLastMessages("x++");
    c.put();

    handler.doCommand(Message.createForTests("jason: s/x/y/"));
    assertEquals(2, xmpp.messages.size());
    assertEquals("[neil] jason: s/x/y/", xmpp.messages.get(0).getBody());
    assertEquals("_neil thinks jason meant y++_",
                 xmpp.messages.get(1).getBody());    
  }
}