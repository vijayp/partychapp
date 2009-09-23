package com.imjasonh.partychapp.server;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.Message;
import com.google.appengine.api.xmpp.MessageBuilder;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.FakeDatastore;
import com.imjasonh.partychapp.MockXMPPService;

public class PartychappServletTest extends TestCase {
  MockXMPPService xmpp = new MockXMPPService();
  PartychappServlet servlet = new PartychappServlet();

  public void setUp() {
    Datastore.setInstance(new FakeDatastore());
    FakeDatastore.instance().clear();
    SendUtil.setXMPP(xmpp);
  }

  public void testIntegration() {
    JID roomJid = new JID("pancake@partychapp.appspotchat.com");
    String[] script = {
      "neil@gmail.com: hi partychat",
      "neil@gmail.com: okay, now the room should exist",
      "jason@gmail.com: i'm joining too!",
      "neil@gmail.com: jason++ for joining",
      "jason@gmail.com: neil-- for bugs",
      "neil@gmail.com: s/jason/intern/",
      "neil@gmail.com: blah blah blah",
      "neil@gmail.com: s/blah/whee/",
      "neil@gmail.com: /alias sanchito",
      "neil@gmail.com: testing new alias",
      "neil@gmail.com: /me hopes dolapo is happy",
      "jason@gmail.com: /alias intern",
      "neil@gmail.com: /list",
      "neil@gmail.com: this is a unicode TM symbol: \u2122",
    };

    String[] expected = {
      "neil@gmail.com: The channel 'pancake' has been created, and you have joined with the alias 'neil'",
      // These messages don't get sent because the room is empty.
      //"-neil@gmail.com: [\"neil\"] hi partychat",
      //"-neil@gmail.com: [\"neil\"] okay, now the room should exist",
      "jason@gmail.com: You have joined 'pancake' with the alias 'jason'",
      "-jason@gmail.com: jason@gmail.com has joined the channel with the alias 'jason'",
      "-jason@gmail.com: [\"jason\"] i'm joining too!",
      "#2: [\"neil\"] jason++ [woot! now at 1] for joining",
      "#2: [\"jason\"] neil-- [ouch! now at -1] for bugs",
      "-neil@gmail.com: [\"neil\"] s/jason/intern/",
      "#2: Undoing original actions: jason++ [back to 0]",
      "#2: _neil meant intern++ [woot! now at 1] for joining_",
      "-neil@gmail.com: [\"neil\"] blah blah blah",
      "-neil@gmail.com: [\"neil\"] s/blah/whee/",
      "#2: _neil meant whee blah blah_",
      "neil@gmail.com: You are now known as 'sanchito'",
      "#2: 'neil' is now known as 'sanchito'",
      "-neil@gmail.com: [\"sanchito\"] testing new alias",
      "-neil@gmail.com: _sanchito hopes dolapo is happy_",
      "jason@gmail.com: You are now known as 'intern'",
      "#2: 'jason' is now known as 'intern'",
      "neil@gmail.com: Listing members of 'pancake'\n* intern (jason@gmail.com)\n* sanchito (neil@gmail.com)",
      "jason@gmail.com: [\"sanchito\"] this is a unicode TM symbol: \u2122",
    };
    
    for (String line : script) {
      String[] splitUp = line.split(": ", 2);
      String sender = splitUp[0];
      String body = splitUp[1];

      servlet.doXmpp(new MessageBuilder()
                         .withBody(body)
                         .withFromJid(new JID(sender))
                         .withRecipientJids(new JID[] { roomJid })
                         .build());
    }

    List<Message> sentMessages = xmpp.messages;
    List<String> expectedMessages = Arrays.asList(expected);
    assertEquals(expectedMessages.size(), sentMessages.size());
    for (int i = 0; i < expectedMessages.size(); ++i) {
      String[] splitUp = expectedMessages.get(i).split(": ", 2);
      String currExpectedRecipients = splitUp[0];
      String currExpectedBody = splitUp[1];
      Message currSent = sentMessages.get(i);
      assertEquals(currExpectedBody, currSent.getBody());
      
      List<JID> actualRecipients = Arrays.asList(currSent.getRecipientJids());
      if (currExpectedRecipients.startsWith("#")) {
        assertEquals(Integer.valueOf(currExpectedRecipients.substring(1)).intValue(),
                     actualRecipients.size());
      } else if (currExpectedRecipients.startsWith("-")) {
        String expectedNotToReceive = currExpectedRecipients.substring(1);
        for (JID jid : actualRecipients) {
          assertNotSame(expectedNotToReceive, jid.getId());
        }
      } else {
        String expectedToReceive = currExpectedRecipients;
        boolean found = false;
        for (JID jid : actualRecipients) {
          if (expectedToReceive.equals(jid.getId())) {
            found = true;
            break;
          }
        }
        assertTrue(expectedToReceive + " should have received " + currExpectedBody, found);
      }
    }
  }
}