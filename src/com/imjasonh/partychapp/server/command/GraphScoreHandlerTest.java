package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.FakeDatastore;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.MockXMPPService;
import com.imjasonh.partychapp.server.SendUtil;

import junit.framework.TestCase;

public class GraphScoreHandlerTest extends TestCase {
  MockXMPPService xmpp = new MockXMPPService();
  PPBHandler ppb = new PPBHandler();
  GraphScoreHandler handler = new GraphScoreHandler();
  
  public void setUp() {
    Datastore.setInstance(new FakeDatastore());
    SendUtil.setXMPP(xmpp);
  }
  
  public void testSimple() {
    ppb.doCommand(Message.createForTests("x++"));
    ppb.doCommand(Message.createForTests("x--"));
    ppb.doCommand(Message.createForTests("x++"));
    ppb.doCommand(Message.createForTests("x++"));
    ppb.doCommand(Message.createForTests("x++"));
    ppb.doCommand(Message.createForTests("x++"));
    ppb.doCommand(Message.createForTests("x--"));
    ppb.doCommand(Message.createForTests("x--"));
    ppb.doCommand(Message.createForTests("x--"));
    ppb.doCommand(Message.createForTests("x--"));
    ppb.doCommand(Message.createForTests("x--"));
    ppb.doCommand(Message.createForTests("x--"));
    ppb.doCommand(Message.createForTests("x--"));
    xmpp.messages.clear();
    
    handler.doCommand(Message.createForTests("/graph-score x"));
    String url = xmpp.messages.get(0).getBody();
    assertEquals("", url);
  }
}