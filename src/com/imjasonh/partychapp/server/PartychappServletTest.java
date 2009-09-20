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
    };

    String[] expected = {
      "neil@gmail.com: The channel 'pancake' has been created, and you have joined with the alias 'neil'",
      "-neil@gmail.com: [\"neil\"] hi partychat",
      "-neil@gmail.com: [\"neil\"] okay, now the room should exist",
      "jason@gmail.com: You have joined 'pancake' with the alias 'jason'",
      "*: jason@gmail.com has joined the channel with the alias 'jason'",
      "-jason@gmail.com: [\"jason\"] i'm joining too!",
      "*: [\"neil\"] jason++ [woot! now at 1] for joining",
      "*: [\"jason\"] neil-- [ouch! now at -1] for bugs",
      "-neil@gmail.com: [\"neil\"] s/jason/intern/",
      "*: Undoing original actions: jason++ [back to 0]",
      "*: _neil meant intern++ [woot! now at 1] for joining_",
      "-neil@gmail.com: [\"neil\"] blah blah blah",
      "-neil@gmail.com: [\"neil\"] s/blah/whee/",
      "*: _neil meant whee blah blah_",
      "neil@gmail.com: You are now known as 'sanchito'",
      "*: 'neil' is now known as 'sanchito'",
      "-neil@gmail.com: [\"sanchito\"] testing new alias",
      "-neil@gmail.com: _sanchito hopes dolapo is happy_",
      "jason@gmail.com: You are now known as 'intern'",
      "*: 'jason' is now known as 'intern'",
      "neil@gmail.com: Listing members of 'pancake'\n* intern (jason@gmail.com)\n* sanchito (neil@gmail.com)",
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
      if (currExpectedRecipients.equals("*")) {
        // TODO(nsanch)
      }
      assertEquals(currExpectedBody, currSent.getBody());
    }
  }
}