package com.imjasonh.partychapp.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.xmpp.JID;
import com.google.appengine.repackaged.com.google.common.collect.Lists;
import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Configuration;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.Message.MessageType;
import com.imjasonh.partychapp.server.command.Command;

@SuppressWarnings("serial")
public class EmailServlet extends HttpServlet {
  private static final Logger LOG = Logger.getLogger(EmailServlet.class.getName());
  
  public static class EmailMessage {
    public InternetAddress from;
    public List<InternetAddress> to;
    public String subject;
    public String body;
  }
  
  public EmailMessage parseToBetterFormat(MimeMessage m) {
    try {
      EmailMessage email = new EmailMessage();
      Address[] senders = m.getFrom();
      if ((senders == null) || senders.length != 1) {
        LOG.severe("ignoring incoming email with null or multiple from's: "
          + senders);
        return null;
      }
      email.from = new InternetAddress(senders[0].toString());
  
      Address[] recipients = m.getAllRecipients();
      if (recipients == null) {
        LOG.severe("ignoring incoming email with null recipient list from sender: "
                   + email.from);
        return null;
      }
      email.to = Lists.newArrayList();
  
      for (Address a : recipients) {
        email.to.add(new InternetAddress(a.toString()));
      }
      
      email.subject = m.getSubject();
      
      String contentType = m.getContentType();
      if (!contentType.startsWith("text/plain;") && !contentType.startsWith("text/html;")) {
        LOG.log(Level.WARNING, "ignoring message with unrecognized content type" + contentType);
        return null;
      }
      try {
        // This is stupid, but I've seen both ByteArrayInputStream and String,
        // and the interface doesn't specify one.
        Object o = m.getContent();
        if (o instanceof String) {
          email.body = (String)o;
        } else if (o instanceof ByteArrayInputStream) {
          ByteArrayInputStream stream = (ByteArrayInputStream)o;
          byte[] bytes = new byte[stream.available()];
          stream.read(bytes);
          email.body = new String(bytes);
        }
      } catch (IOException e) {
        LOG.log(Level.SEVERE, "Caught exception while trying to read content of email from " + email.from, e);
        return null;
      }
      
      return email;
    } catch (MessagingException e) {
      LOG.log(Level.SEVERE, "Couldn't parse incoming email", e);
      return null;
    }
  }
  
  public Message extractPchappMessageFromEmail(EmailMessage email, InternetAddress to) {
    String recipient = to.getAddress();
    // HACK
    if (recipient.equals("dogfood.pancake@gmail.com")) {
      recipient = "dogfood@partychapp.appspotmail.com";
    }
    if (!recipient.endsWith(Configuration.mailDomain)) {
      LOG.log(Level.SEVERE, "ignoring incoming email with unrecognized domain in to: " + recipient);
      return null;
    }
    String channelName = recipient.split("@")[0];

    Channel channel = Datastore.instance().getChannelByName(channelName);
    if (channel == null) {
      LOG.warning("unknown channel " + channelName + " from email sent to " + recipient);
      return null;
    }
    
    String memberPhoneNumber = tryExtractPhoneNumber(email.from.getAddress());
    if (memberPhoneNumber == null) {
      Member member = channel.getMemberByJID(new JID(email.from.getAddress()));
      if (member == null) {
        LOG.warning("unknown user " + email.from + " in channel " + channelName);
        return null;
      }
      String content = "Subject: " + email.from;
      if (!email.body.isEmpty()) {
        content  += " / Body: " + email.body;
      }
      
      return new Message(null,
                         new JID(member.getJID()),
                         channel.serverJID(),
                         member,
                         channel,
                         null,
                         MessageType.EMAIL);
    } else {
      // member might be null if we don't know the phone number
      Member member = channel.getMemberByPhoneNumber(memberPhoneNumber);
      if ((member == null) && memberPhoneNumber.startsWith("1")) {
        member = channel.getMemberByPhoneNumber(memberPhoneNumber.substring(1));
      }
      
      // GV emails have a --\nGoogle Voice footer, so try and look for that.
      int end = email.body.indexOf("\n--\n");
      String content = null;
      if (end != -1) {
        content = email.body.substring(0, end).trim();
      } else {
        content = email.body;
      }
      
      return new Message(content,
                         member != null ? new JID(member.getJID()) : null,
                         channel.serverJID(),
                         member,
                         channel,
                         memberPhoneNumber,
                         MessageType.SMS);
    }
  }

  public void doPost(HttpServletRequest req, 
                     HttpServletResponse resp) 
          throws IOException {
    Datastore.instance().startRequest();

    MimeMessage mime = null;
    try {
      mime = new MimeMessage(Session.getDefaultInstance(new Properties(), null),
                             req.getInputStream());
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "error while parsing incoming email", e);
      return;
    }

    EmailMessage email = parseToBetterFormat(mime);

    for (InternetAddress ia : email.to) {
      Message msg = extractPchappMessageFromEmail(email, ia);
      if (msg != null) {
        Command.getCommandHandler(msg).doCommand(msg);
      }
    }
    
    Datastore.instance().endRequest();
  }
  
  public String tryExtractPhoneNumber(String email) {
    // extract 19178041000 from 16464623000.19178041000.somehash@txt.voice.google.com
    if (!email.endsWith("@txt.voice.google.com")) {
      return null;
    }
    
    String firstPart = email.split("@")[0];
    int start = firstPart.indexOf(".");
    int end = firstPart.indexOf(".", start + 1);
    if ((start == -1) || (end == -1)) {
      return null;
    }
    return firstPart.substring(start + 1, end);
  }
}
