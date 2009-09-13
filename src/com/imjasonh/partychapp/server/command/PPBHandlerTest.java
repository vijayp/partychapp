package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.FakeDatastore;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.MockXMPPService;
import com.imjasonh.partychapp.server.SendUtil;

import junit.framework.TestCase;

public class PPBHandlerTest extends TestCase {
  MockXMPPService xmpp = new MockXMPPService();
  PPBHandler ppb = new PPBHandler();
  
  public void setUp() {
    Datastore.setInstance(new FakeDatastore());
    SendUtil.setXMPP(xmpp);
  }
  
  public void assertResponse(String in, String out) {
    ppb.doCommand(Message.createForTests(in));

    assertEquals(1, xmpp.messages.size());
    assertEquals(out, xmpp.messages.get(0).getBody());
  }
  
  public void testSimple() {
    assertResponse("mihai++ for knowing things",
                   "[\"neil\"] mihai++ [woot! now at 1] for knowing things");
  }

  public void testInlineEdit() {
    assertResponse("whee x++ and y-- boo",
                   "[\"neil\"] whee x++ [woot! now at 1] and y-- [ouch! now at -1] boo");
  }
  
  public void testNothingAtEnds() {
    assertResponse("x++ y-- z++",
                   "[\"neil\"] x++ [woot! now at 1] y-- [ouch! now at -1] z++ [woot! now at 1]");
  }
}
