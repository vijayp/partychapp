package com.imjasonh.partychapp.server;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.mail.MailService;
import com.google.appengine.api.mail.MailServiceFactory;

public class MailUtil {
  private static MailService instance = null;
  
  private static final Logger LOG = Logger.getLogger(MailUtil.class.getName());
  
  public static MailService instance() {
    if (instance == null) {
      instance = MailServiceFactory.getMailService();
    }
    return instance;
  }
  
  public static void setMailService(MailService newInstance) {
    instance = newInstance;
  }
  
  public static String sendMail(String subject,
                              String body,
                              String recipient) {
    MailService.Message email = new MailService.Message("partychat@gmail.com",
                                                        recipient,
                                                        subject,
                                                        body);
    
    try {
      instance().send(email);
    } catch (IOException e) {
      LOG.log(Level.SEVERE,
              "Caught exception while trying to send email to " +
                 recipient,
              e);
      String reply = "Error while sending mail to '" + recipient + "'. Email may not have been sent.";
      return reply;
    }
    return null;
  }
  
  private MailUtil() {}
}
