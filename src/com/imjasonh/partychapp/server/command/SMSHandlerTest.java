package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.FakeDatastore;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.MockXMPPService;
import com.imjasonh.partychapp.Message.MessageType;
import com.imjasonh.partychapp.server.SendUtil;

import junit.framework.TestCase;

public class SMSHandlerTest extends TestCase {
  SMSHandler handler = new SMSHandler();
  MockXMPPService xmpp = new MockXMPPService();
  
  public void setUp() {
    Datastore.setInstance(new FakeDatastore());
    SendUtil.setXMPP(xmpp);
  }

  public void testSimple() {
    Message m = Message.createForTests("this is a text message");
    m.messageType = MessageType.SMS;
    m.phoneNumber = "16464623000";
    
    handler.doCommand(m);
    
    assertEquals(1, xmpp.messages.size());
    assertEquals("**via SMS (16464623000)** [neil] this is a text message", xmpp.messages.get(0).getBody());
  }
  
  public void testNoMember() {
    Message m = Message.createForTests("this is a text message");
    m.messageType = MessageType.SMS;
    m.phoneNumber = "16464623000";
    m.member = null;
    
    handler.doCommand(m);

    assertEquals(1, xmpp.messages.size());
    assertEquals("**via SMS (16464623000)** [no member found] this is a text message", xmpp.messages.get(0).getBody());
  }
}
