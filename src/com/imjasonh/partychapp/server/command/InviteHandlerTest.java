package com.imjasonh.partychapp.server.command;

import java.util.List;

import junit.framework.TestCase;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.FakeDatastore;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.MockMailService;
import com.imjasonh.partychapp.MockXMPPService;
import com.imjasonh.partychapp.server.MailUtil;
import com.imjasonh.partychapp.server.SendUtil;

public class InviteHandlerTest extends TestCase {
  InviteHandler handler = new InviteHandler();
  MockXMPPService xmpp = new MockXMPPService();
  MockMailService mailer = new MockMailService();
  
  public void setUp() {
    Datastore.setInstance(new FakeDatastore());
    SendUtil.setXMPP(xmpp);
    MailUtil.setMailService(mailer);
  }
  
  public void testInviteAndEmailIsSent() {
    handler.doCommand(Message.createForTests("/invite dan@gmail.com"));
    List<String> invited = FakeDatastore.instance().fakeChannel().getInvitees();
    assertEquals(1, invited.size());
    assertEquals("dan@gmail.com", invited.get(0));
    
    assertEquals(1, xmpp.messages.size());
    assertEquals("_neil invited dan@gmail.com_", xmpp.messages.get(0).getBody());
    
    assertEquals(1, xmpp.invited.size());
    assertEquals("dan@gmail.com", xmpp.invited.get(0).getId());
    
    assertEquals(1, mailer.sentMessages.size());
    assertEquals("neil invited you to 'pancake'",
                 mailer.sentMessages.get(0).getSubject());
    assertTrue("Actual Message: " + mailer.sentMessages.get(0).getTextBody(),
               mailer.sentMessages.get(0).getTextBody().startsWith(
                   "neil (neil@gmail.com) invited you to a chatroom named 'pancake'"));
    assertEquals("partychat@gmail.com",
                 mailer.sentMessages.get(0).getSender());
  }
}