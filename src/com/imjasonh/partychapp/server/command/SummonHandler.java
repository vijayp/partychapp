package com.imjasonh.partychapp.server.command;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.mail.MailService;
import com.google.appengine.api.mail.MailServiceFactory;
import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.Message;

public class SummonHandler extends SlashCommand {
  private static final Logger LOG = Logger.getLogger(SummonHandler.class.getName());
  
  private MailService mailer = MailServiceFactory.getMailService();
  private BroadcastHandler bcast = new BroadcastHandler();
  
  public SummonHandler() {
    super("summon");
  }
  
  public void setMailService(MailService mailer) {
    this.mailer = mailer;
  }

  @Override
  public void doCommand(Message msg, String argument) {
    bcast.doCommand(msg);
    
    Member toSummon = msg.channel.getMemberByAlias(argument.trim());
    if (toSummon == null) {
      String reply = "Could not find member with alias '" + argument.trim() + "'";
      msg.channel.broadcast(reply, msg.member);
      return;
    }
    String emailBody = msg.member.getAlias() + " has summoned you to '" + msg.channel.getName() + "'.";
    MailService.Message email = new MailService.Message("partychat@gmail.com",
                                                        toSummon.getEmail(),
                                                        "You have been summoned to '" + msg.channel.getName() + "'",
                                                        emailBody);
    
    String reply = "_" + msg.member.getAlias() + " summoned " + toSummon.getAlias() + "_";
    try {
      mailer.send(email);
    } catch (IOException e) {
      LOG.log(Level.SEVERE,
              "Caught exception while trying to summon " +
                 toSummon.getAlias() + " with the email address " +
                 toSummon.getEmail(),
              e);
      reply = "Error while summoning '" + toSummon.getAlias() + "' to room. Email may not have been sent.";
    }
    msg.channel.broadcastIncludingSender(reply);
  }

  public String documentation() {
    return "/summon <alias> - summons a person in the room by sending them an email.";
  }

}
