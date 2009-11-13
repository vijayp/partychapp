package com.imjasonh.partychapp.server.command;

import junit.framework.TestCase;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.FakeDatastore;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.MockXMPPService;
import com.imjasonh.partychapp.Message.MessageType;
import com.imjasonh.partychapp.server.SendUtil;

public class EmailHandlerTest extends TestCase {
  EmailHandler handler = new EmailHandler();
  MockXMPPService xmpp = new MockXMPPService();
  
  public void setUp() {
    Datastore.setInstance(new FakeDatastore());
    SendUtil.setXMPP(xmpp);
  }

  public void testSimple() {
    Message m = Message.createForTests("Subject: x / Body: blah blah");
    m.messageType = MessageType.EMAIL;
    
    handler.doCommand(m);
    
    assertEquals(1, xmpp.messages.size());
    assertEquals("**via email** [neil] Subject: x / Body: blah blah", xmpp.messages.get(0).getBody());
  }
}
