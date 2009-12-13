package com.imjasonh.partychapp.server;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.Message;
import com.google.appengine.api.xmpp.MessageBuilder;
import com.google.appengine.repackaged.com.google.common.collect.Lists;
import com.imjasonh.partychapp.Configuration;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.FakeDatastore;
import com.imjasonh.partychapp.MockMailService;
import com.imjasonh.partychapp.MockXMPPService;

public class PartychappServletTest extends TestCase {
  MockXMPPService xmpp = new MockXMPPService();
  PartychappServlet servlet = new PartychappServlet();

  public void setUp() {
	FakeDatastore datastore = new FakeDatastore();
	Datastore.setInstance(datastore);
	datastore.setUp();
	FakeDatastore.fakeChannel().delete();
    SendUtil.setXMPP(xmpp);
    MailUtil.setMailService(new MockMailService());
  }
  
  class TestMessage {
    boolean incoming;
    String userString;
    String content;
    JID serverJID;
    
    public TestMessage(boolean incoming, String userString, String content) {
      this.incoming = incoming;
      this.userString = userString;
      this.content = content;
      this.serverJID = new JID("pancake@" + Configuration.chatDomain);
    }
    
    public Message toIncomingMessage() {
      assertTrue(incoming);
      return new MessageBuilder()
        .withBody(content)
        .withFromJid(new JID(userString))
        .withRecipientJids(new JID[] { serverJID })
        .build();
    }
    
    public void assertSameAs(Message actual) {
      assertEquals(content, actual.getBody());

      List<JID> actualRecipients = Arrays.asList(actual.getRecipientJids());
      if (userString.startsWith("#")) {
        int expectedNum = Integer.valueOf(userString.substring(1));
        assertEquals(expectedNum, actualRecipients.size());
      } else if (userString.startsWith("-")) {
        String expectedNotToReceive = userString.substring(1);
        for (JID jid : actualRecipients) {
          assertNotSame(expectedNotToReceive, jid.getId());
        }
      } else {
        assertEquals(1, actualRecipients.size());
        assertEquals(userString, actualRecipients.get(0).getId());
      }
      
      assertEquals(serverJID.getId(), actual.getFromJid().getId());
    }
    
    public String toString() {
      return "[ TestMessage: " + incoming + ", " +
          userString + ", " +
          "'" + content + "' ]"; 
    }
  };
  
  public void testIntegration() {
    TestMessage script[] = {
      new TestMessage(true, "neil@gmail.com", "hi partychat"),
      new TestMessage(false, "neil@gmail.com", "The channel 'pancake' has been created, and you have joined with the alias 'neil'"),
      // doesn't get sent because the room is empty.
      //"-neil@gmail.com", "[neil]s hi partychat"),

      new TestMessage(true, "neil@gmail.com", "okay, now the room should exist"),
      // doesn't get sent because the room is empty.
      //"-neil@gmail.com", "[neil] okay, now the room should exist"),

      new TestMessage(true, "jason@gmail.com", "i'm joining too!"),
      new TestMessage(false, "jason@gmail.com", "You have joined 'pancake' with the alias 'jason'"),
      new TestMessage(false, "-jason@gmail.com", "jason@gmail.com has joined the channel with the alias 'jason'"),
      new TestMessage(false, "-jason@gmail.com", "[jason] i'm joining too!"),

      new TestMessage(true, "neil@gmail.com", "jason++ for joining"),
      new TestMessage(false, "#2", "[neil] jason++ [woot! now at 1] for joining"),

      new TestMessage(true, "jason@gmail.com", "neil-- for bugs"),
      new TestMessage(false, "#2", "[jason] neil-- [ouch! now at -1] for bugs"),
      
      new TestMessage(true, "neil@gmail.com", "s/jason/intern/"),
      new TestMessage(false, "-neil@gmail.com", "[neil] s/jason/intern/"),
      new TestMessage(false, "#2", "Undoing original actions: jason++ [back to 0]"),
      new TestMessage(false, "#2", "_neil meant intern++ [woot! now at 1] for joining_"),

      new TestMessage(true, "neil@gmail.com", "blah blah blah"),      
      new TestMessage(false, "-neil@gmail.com", "[neil] blah blah blah"),

      new TestMessage(true, "neil@gmail.com", "s/blah/whee/"),
      new TestMessage(false, "-neil@gmail.com", "[neil] s/blah/whee/"),
      new TestMessage(false, "#2", "_neil meant whee blah blah_"),

      new TestMessage(true, "neil@gmail.com", "/alias sanchito"),
      new TestMessage(false, "neil@gmail.com", "You are now known as 'sanchito'"),
      new TestMessage(false, "#2", "'neil' is now known as 'sanchito'"),
      
      new TestMessage(true, "neil@gmail.com", "testing new alias"),
      new TestMessage(false, "-neil@gmail.com", "[sanchito] testing new alias"),
      
      new TestMessage(true, "neil@gmail.com", "/me hopes dolapo is happy"),
      new TestMessage(false, "#2", "_sanchito hopes dolapo is happy_"),
      
      new TestMessage(true, "jason@gmail.com", "/alias intern"),
      new TestMessage(false, "jason@gmail.com", "You are now known as 'intern'"),
      new TestMessage(false, "#2", "'jason' is now known as 'intern'"),

      new TestMessage(true, "neil@gmail.com", "/list"),
      new TestMessage(false, "neil@gmail.com", "Listing members of 'pancake'\n* intern (jason@gmail.com)\n* sanchito (neil@gmail.com)"),

      new TestMessage(true, "neil@gmail.com", "this is a unicode TM symbol: \u2122"),
      new TestMessage(false, "jason@gmail.com", "[sanchito] this is a unicode TM symbol: \u2122"),

      new TestMessage(true, "kushal@kushaldave.com", "now i'm joining"),
      new TestMessage(false, "kushal@kushaldave.com", "You have joined 'pancake' with the alias 'kushal'"),
      new TestMessage(false, "-kushal@kushaldave.com", "kushal@kushaldave.com has joined the channel with the alias 'kushal'"),
      new TestMessage(false, "-kushal@kushaldave.com", "[kushal] now i'm joining"),

      new TestMessage(true, "kushal@kushaldave.com", "/inviteonly"),
      new TestMessage(false, "#3", "_kushal set the room to invite-only._"),

      new TestMessage(true, "david@gmail.com", "i'll try to join but i haven't been invited"),
      new TestMessage(false, "david@gmail.com", "You must be invited to this room."),

      new TestMessage(true, "kushal@kushaldave.com", "/invite david@gmail.com"),
      new TestMessage(false, "#3", "_kushal invited david@gmail.com_"),

      new TestMessage(true, "david@gmail.com", "yay, now i can join"),
      new TestMessage(false, "david@gmail.com", "You have joined 'pancake' with the alias 'david'"),
      new TestMessage(false, "-david@gmail.com", "david@gmail.com has joined the channel with the alias 'david'"),
      new TestMessage(false, "-david@gmail.com", "[david] yay, now i can join"),

      new TestMessage(true, "david@gmail.com", "/status"),
      new TestMessage(false, "david@gmail.com", "You are currently in 'pancake' as 'david.'"),

      new TestMessage(true, "david@gmail.com", "radioheda++"),
      new TestMessage(false, "#4", "[david] radioheda++ [woot! now at 1]"),

      new TestMessage(true, "david@gmail.com", "/undo"),
      new TestMessage(false, "-david@gmail.com", "[david] /undo"),
      new TestMessage(false, "#4", "Undoing original actions: radioheda++ [back to 0]"),

      new TestMessage(true, "neil@gmail.com", "/debug sequenceIds"),
      new TestMessage(false, "neil@gmail.com", "enabling sequenceIds for you"),

      new TestMessage(true, "jason@gmail.com", "test with sequenceIds on"),
      new TestMessage(false, "#2", "[intern] test with sequenceIds on"),
      new TestMessage(false, "neil@gmail.com", "[intern] test with sequenceIds on (27)"),

      new TestMessage(true, "neil@gmail.com", "/debug"),
      new TestMessage(false, "neil@gmail.com", "Your current debug options are: [sequenceIds]"),

      new TestMessage(true, "jason@gmail.com", "test2++ with sequenceIds on"),
      new TestMessage(false, "#3", "[intern] test2++ [woot! now at 1] with sequenceIds on"),
      new TestMessage(false, "neil@gmail.com", "[intern] test2++ [woot! now at 1] with sequenceIds on (28)"),

      new TestMessage(true, "neil@gmail.com", "/me is having fun with sequenceIds"),
      new TestMessage(false, "#3", "_sanchito is having fun with sequenceIds_"),
      new TestMessage(false, "neil@gmail.com", "_sanchito is having fun with sequenceIds_ (29)"),

      new TestMessage(true, "neil@gmail.com", "/debug clear"),
      new TestMessage(false, "neil@gmail.com", "clearing all debug options"),

      new TestMessage(true, "jason@gmail.com", "test with sequenceIds off"),
      new TestMessage(false, "#3", "[intern] test with sequenceIds off"),
      
      new TestMessage(true, "jason@gmail.com", "/alias jason--"),
      new TestMessage(false, "jason@gmail.com", "You are now known as 'jason--'"),
      new TestMessage(false, "#4", "'intern' is now known as 'jason--'"),

      new TestMessage(true, "neil@gmail.com", "/summon jason--"),
      new TestMessage(false, "#3", "[sanchito] /summon jason--"),
      new TestMessage(false, "#4", "_sanchito summoned jason--_"),
    };

    for (int i = 0; i < script.length;) {
      TestMessage line = script[i];
      servlet.doXmpp(line.toIncomingMessage());

      List<Message> sentMessages = xmpp.messages;
      List<TestMessage> expectedMessages = Lists.newArrayList();
      for (++i; (i < script.length) && !script[i].incoming; ++i) {
        expectedMessages.add(script[i]);
      }
      
      assertEquals("wrong number of messages sent for input line '" + line.content + ".'",
                   expectedMessages.size(), sentMessages.size());
      for (int it = 0; it < expectedMessages.size(); ++it) {
        TestMessage expected = expectedMessages.get(it);
        Message sent = sentMessages.get(it);
        expected.assertSameAs(sent);
      }
      xmpp.messages.clear();
    }
  }
}