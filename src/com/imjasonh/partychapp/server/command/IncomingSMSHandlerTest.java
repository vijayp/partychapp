package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.Message.MessageType;

public class IncomingSMSHandlerTest extends CommandHandlerTest {
  IncomingSMSHandler handler = new IncomingSMSHandler();

  public void testSimple() {
    Message m = Message.createForTests("this is a text message");
    m.messageType = MessageType.SMS;
    m.phoneNumber = "16464623000";
    
    handler.doCommand(m);
    
    assertEquals(1, xmpp.messages.size());
    assertEquals("*via SMS* [neil] this is a text message", xmpp.messages.get(0).getBody());
  }
}
