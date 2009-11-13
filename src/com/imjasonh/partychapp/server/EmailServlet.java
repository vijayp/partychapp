package com.imjasonh.partychapp.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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
import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Configuration;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.Message.MessageType;
import com.imjasonh.partychapp.server.command.Command;

@SuppressWarnings("serial")
public class EmailServlet extends HttpServlet {
  private static final Logger LOG = Logger.getLogger(EmailServlet.class.getName());

  public void doPost(HttpServletRequest req, 
                     HttpServletResponse resp) 
          throws IOException {
    try {
      Datastore.instance().startRequest();
  
      Properties props = new Properties(); 
      Session session = Session.getDefaultInstance(props, null);
      try {
        MimeMessage message = new MimeMessage(session, req.getInputStream());
        Address[] senders = message.getFrom();
        if ((senders == null) || senders.length != 1) {
          LOG.severe("ignoring incoming email with null or multiple from's: " + senders);
          return;
        }
        String sender = (new InternetAddress(senders[0].toString())).getAddress();
  
        Address[] recipients = message.getAllRecipients();
        if (recipients == null) {
          LOG.severe("ignoring incoming email with null recipient list from sender: " + sender);
        }
  
        for (Address a : recipients) {
          // is this right?
          InternetAddress ia = new InternetAddress(a.toString());
          String emailAddress = ia.getAddress();
          if (!emailAddress.endsWith(Configuration.mailDomain)) {
            LOG.log(Level.SEVERE, "ignoring incoming email with unrecognized domain in to: " + emailAddress);
            continue;
          }
          String channelName = emailAddress.split("@")[0];
  
          String contentType = message.getContentType();
          if (!contentType.startsWith("text/plain;") && !contentType.startsWith("text/html;")) {
            LOG.log(Level.WARNING, "ignoring message with unrecognized content type" + contentType);
            return;
          }
          ByteArrayInputStream stream = (ByteArrayInputStream)message.getContent();
          byte[] bytes = new byte[stream.available()];
          stream.read(bytes);
          String content = new String(bytes);
          String body = message.getSubject();
          if (!content.isEmpty()) {
            body += " / Body: " + content;
          }

          Channel channel = Datastore.instance().getChannelByName(channelName);
          if (channel == null) {
            LOG.warning("unknown channel " + channelName + " from email sent to " + emailAddress);
            continue;
          }
          String memberPhoneNumber = tryExtractPhoneNumber(sender);
          Member member = null;
          MessageType messageType = MessageType.EMAIL;
          if (memberPhoneNumber == null) {
            member = channel.getMemberByJID(new JID(sender));
          } else {
            member = channel.getMemberByPhoneNumber(memberPhoneNumber);
            messageType = MessageType.SMS;
          }
          if (messageType.equals(MessageType.EMAIL) && member == null) {
            LOG.warning("unknown user " + sender + " in channel " + channelName);
            continue;
          }
          com.imjasonh.partychapp.Message msg = new com.imjasonh.partychapp.Message(body,
                                                                                    new JID(member.getJID()),
                                                                                    channel.serverJID(),
                                                                                    member,
                                                                                    channel,
                                                                                    messageType);
          if (memberPhoneNumber != null) {
            msg.phoneNumber = memberPhoneNumber;
          }
          Command.getCommandHandler(msg).doCommand(msg);
        }
      } catch (MessagingException e) {
        LOG.log(Level.SEVERE, "Couldn't parse incoming email", e);
        return;
      }
    } finally {
      Datastore.instance().endRequest();
    }
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
