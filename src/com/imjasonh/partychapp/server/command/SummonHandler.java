package com.imjasonh.partychapp.server.command;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.mail.MailService;
import com.google.appengine.api.mail.MailServiceFactory;
import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.server.SendUtil;

public class SummonHandler extends SlashCommand {
  private static final Logger LOG = Logger.getLogger(SummonHandler.class.getName());
  
  private MailService mailer = MailServiceFactory.getMailService();
  private BroadcastHandler bcast = new BroadcastHandler();
  
  public SummonHandler() {
    super("summon");
  }

  @Override
  public void doCommand(Message msg, String argument) {
    bcast.doCommand(msg);
    
    Member toSummon = msg.channel.getMemberByAlias(argument.trim());
    if (toSummon == null) {
      String reply = "Could not find member with alias '" + argument.trim() + "'";
      SendUtil.broadcast(reply, msg.channel, msg.serverJID, msg.userJID);
      return;
    }
    String emailBody = msg.member.getAlias() + " has summoned you to '" + msg.channel.getName() + "'.";
    // TODO(nsanch): add partychat@gmail.com as an admin so we can use it as the From:.
    MailService.Message email = new MailService.Message("nsanch@gmail.com",
                                                        toSummon.getEmail(),
                                                        "You have been summoned to '" + msg.channel.getName() + "'",
                                                        emailBody);
    try {
      mailer.send(email);
    } catch (IOException e) {
      LOG.log(Level.SEVERE,
              "Caught exception while trying to summon " +
                 toSummon.getAlias() + " with the email address " +
                 toSummon.getEmail(),
              e);
    }
    String reply = "_" + msg.member.getAlias() + " summoned '" + toSummon.getAlias() + "'_";
    SendUtil.broadcastIncludingSender(reply, msg.channel, msg.serverJID);
  }

  public String documentation() {
    // TODO Auto-generated method stub
    return null;
  }

}
