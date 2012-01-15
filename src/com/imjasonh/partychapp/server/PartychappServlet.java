package com.imjasonh.partychapp.server;

import com.google.appengine.api.quota.QuotaService;
import com.google.appengine.api.quota.QuotaServiceFactory;
import com.google.appengine.api.quota.QuotaService.DataType;
import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.Message;
import com.google.appengine.api.xmpp.MessageBuilder;
import com.google.appengine.api.xmpp.XMPPService;
import com.google.appengine.api.xmpp.XMPPServiceFactory;


import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Datastore.NotImplementedException;
import com.imjasonh.partychapp.Configuration;
import com.imjasonh.partychapp.LiveDatastore;
import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.PersistentConfiguration;
import com.imjasonh.partychapp.User;
import com.imjasonh.partychapp.Message.MessageType;
import com.imjasonh.partychapp.server.command.Command;
import com.imjasonh.partychapp.server.command.CommandHandler;
import com.imjasonh.partychapp.server.live.ChannelUtil;
import com.imjasonh.partychapp.stats.ChannelStats;

import java.io.IOException;
import java.util.Collections;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

@SuppressWarnings("serial")
public class PartychappServlet extends HttpServlet {
  
  // TODO(vijayp): replace this with persistent config data 
  public final static String PROXY_DEFAULT_SUBDOMAIN = "im";
  public final static String PARTYCHAPP_DOMAIN = "partychapp.appspotchat.com";
  
  public static String getMigratedDomain(String domain) {
    // TODO Auto-generated method stub
    if (domain == null || domain.isEmpty()) {
      domain = PartychappServlet.PROXY_DEFAULT_SUBDOMAIN;
    }
    
    // TODO(vijayp): pull this from persistent store:
    return domain + ".partych.at";
  }
  public static String URLForDomain(String domain) {
    // TODO: pull this from persistent:
    return "https://" + PartychappServlet.getMigratedDomain(domain)+ "/___control___";
  }
  
 public final static String OVERLOADED_MESSAGE = 
      "System is currently overloaded. Please check" +
          " http://partych.at for details";

  private static final Logger logger =
      Logger.getLogger(PartychappServlet.class.getName());

  private static final XMPPService XMPP = XMPPServiceFactory.getXMPPService();
  private static final QuotaService QS = QuotaServiceFactory.getQuotaService();
  private static final Pattern[] jidBlacklist = {
    // Bots that talk to each other and cause loops
    Pattern.compile("guru@googlelabs\\.com(/.*)?", Pattern.CASE_INSENSITIVE),
    Pattern.compile("webtoim@gmail\\.com(/.*)?", Pattern.CASE_INSENSITIVE),
    Pattern.compile(".*@bot.talk.google\\.com(/.*)?", Pattern.CASE_INSENSITIVE),
    Pattern.compile("service@gtalk2voip\\.com(/.*)?", Pattern.CASE_INSENSITIVE),
    Pattern.compile("en@clisearch.net(/.*)?", Pattern.CASE_INSENSITIVE),

    // Various other bots that we've encountered
    Pattern.compile(".*(?:g2twit[.]appspotchat[.]com|twitalker\\d+@appspot[.]com|chitterim@appspot[.]com|tweetjid@appspot[.]com|twiyia@gmail[.]com|roomchinese[.]appspotchat[.]com|353606@gmail[.]com).*", Pattern.CASE_INSENSITIVE),
  };


  @Override
  @Deprecated
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    long startCpu = -1;
    if (QS.supports(DataType.CPU_TIME_IN_MEGACYCLES)) {
      startCpu = QS.getCpuTimeInMegaCycles();
    }

    Message xmppMessage;
    try {
      xmppMessage = XMPP.parseMessage(req);
    } catch (IllegalArgumentException e) {
      // These exceptions are apparently caused by a bug in the gtalk flash
      // gadget, so let's just ignore them.
      // http://code.google.com/p/googleappengine/issues/detail?id=2082
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    try { // FIXME: huge hack
      final String fromAddr = xmppMessage.getFromJid().getId();
      for (Pattern p : jidBlacklist) {
        if (p.matcher(fromAddr).matches()) {
          logger.info("blocked message from " + fromAddr + " to channel " +
              ((xmppMessage.getRecipientJids().length > 0) ? jidToLowerCase(xmppMessage
                  .getRecipientJids()[0]) : "NONE") + " due to ACL " + p.toString());
          resp.sendError(HttpServletResponse.SC_FORBIDDEN);
          return;
        }
      }
    } catch (Exception e) {
      logger.warning("unknown exception on ACL " + e);
    }
    final String toAddr   = ((xmppMessage.getRecipientJids().length > 0) 
        ? jidToLowerCase(xmppMessage.getRecipientJids()[0]).getId() : "");
    final String channelName = toAddr.split(("@"))[0];

    try {
      final String fromAddr = jidToLowerCase(xmppMessage.getFromJid()).getId();


      //logger.info("comparing <" + fromAddr + "> to <"+ PROXY_CONTROL);
      //logger.info("comparing <" + toAddr + "> to <"+ PARTYCHAPP_CONTROL);
      Datastore datastore = Datastore.instance();
      datastore.startRequest();
      Channel c = datastore.getChannelByName(channelName);
      datastore.endRequest();
      if (c!= null && c.getMembers().size() > 300) {
        logger.info("rejected inbound message for " + c.getName() 
            + " because it is too large");
        boolean succ = ChannelUtil.sendMessage(
            "channels with more than 300 users are temporarily not supported",
            fromAddr,
            toAddr);
        return;
      } else if (
          c != null && 
          (c.getName().equals("dogfood") 
              || c.shouldMigrate())
          ) {
        if (c.isMigrated()) {
          ; // no reason to change state.
        } else {
          
          // this is a new migration. Do the migration, then send a message
          // to the new channel
          c.setMigrated(true);
          datastore.startRequest();
          c.put();
          datastore.endRequest();
          c.broadcastIncludingSender("This is your new channel");
          logger.info(" NEW MIGRATION for channel " + c.getName());
        }
        // we don't send messages anymore via appspotchat.
        logger.info(c.getName() + " channel is already migrated. Bad user: " + fromAddr);
        return;
      } else {
        
        // still unmigrated channel
        doXmpp(xmppMessage, null);        

      }


    } finally {
      if (QS.supports(DataType.CPU_TIME_IN_MEGACYCLES) && xmppMessage != null) {
        long endCpu = QS.getCpuTimeInMegaCycles();
        JID serverJID = jidToLowerCase(xmppMessage.getRecipientJids()[0]);
        ChannelStats.recordChannelCpu(channelName, endCpu - startCpu);
      }
    }
  }

  protected static void doControlPacket(Message xmppMessage) throws JSONException {
    String body = xmppMessage.getBody().trim();
    doControlPacket(body,null);
  }
  protected static void doControlPacket(String body, HttpServletResponse resp) throws JSONException {
    logger.info("GOT CONTROL PACKET");
    // json decode the control packet from the body
    // make the new message.
    // doxmpp

    JSONObject jso = new JSONObject(body);
    String state = jso.getString("state");
    //TODO(vijayp): make this a lot nicer and get rid of magic strings, etc..
    // if the state is 'new', we have to send an empty message for every migrated channel
    // this is to prevent the proxy from having to store state (i.e. roster).

    if ((null != state) && state.equals("new")) {
      logger.warning("Looks like the proxy just came up. Refreshing his roster");
      Datastore datastore = new LiveDatastore();
      logger.warning("Looks like the proxy just came up. Refreshing his roster");

      datastore.startRequest();

      try {
        Iterable<Channel> cnls = datastore.getChannelsByMigrationStatus(true);
        for (Channel c: cnls) {
          // send a message to this channel
          logger.info("Sending message to " + c.getName());
          c.broadcastIncludingSender("");
        }

      } catch (NotImplementedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } finally {
        datastore.endRequest();
      }

    } else {
      String decodedTo = jso.getString("to_str");
      decodedTo = decodedTo.split("@")[0] + "@" + PARTYCHAPP_DOMAIN;
      String decodedFrom = jso.getString("from_str");
      if (decodedFrom.contains("im.partych.at")) {
        logger.warning("message is from another channel! rejecting:" +
                       decodedFrom +decodedTo + jso.getString("message_str"));
        return;
      }
      String decodedMsg = jso.getString("message_str");
      Message payload = new MessageBuilder().withFromJid(new JID(decodedFrom))
          .withBody(decodedMsg)
          .withMessageType(com.google.appengine.api.xmpp.MessageType.CHAT)
          .withRecipientJids(new JID(decodedTo)).build();
      logger.info("calling doxmpp with payload :: "
          + payload.toString());
      doXmpp(payload, resp);
    }
  }

  private static JID jidToLowerCase(JID in) {
    return new JID(in.getId().toLowerCase());
  }

  public static void doXmpp(Message xmppMessage, HttpServletResponse resp) {

    long startTime = System.currentTimeMillis();
    Datastore datastore = Datastore.instance();
    datastore.startRequest();

    try {
      JID userJID = jidToLowerCase(xmppMessage.getFromJid());

      // should only be "to" one JID, right?
      JID serverJID = jidToLowerCase(xmppMessage.getRecipientJids()[0]);
      String channelName = serverJID.getId().split("@")[0];

      logger.info("Request by " + userJID.getId() + " for channel " + channelName);

      String body = xmppMessage.getBody().trim();

      if (channelName.equalsIgnoreCase("echo")) {
        handleEcho(xmppMessage);
        return;
      }

      Channel channel = datastore.getChannelByName(channelName);
      Member member = null; 
      if (channel != null) {
        member = channel.getMemberByJID(userJID);
      }
      User user = datastore.getOrCreateUser(userJID.getId().split("/")[0]);

      com.imjasonh.partychapp.Message message =
          new com.imjasonh.partychapp.Message.Builder()
      .setContent(body)
      .setUserJID(userJID)
      .setServerJID(serverJID)
      .setChannel(channel)
      .setMember(member)
      .setUser(user)
      .setMessageType(MessageType.XMPP)
      .build();

      CommandHandler ch = Command.getCommandHandler(message);
          logger.info("Command handler: " + ch + " and resp: " + resp);      
      ch.doCommand(message, resp);

      // {@link User#fixUp} can't be called by {@link FixingDatastore}, since
      // it can't know what channel the user is currently messaging, so we have
      // to do it ourselves.
      user.fixUp(message.channel);
      user.maybeMarkAsSeen();

      long requestTime = System.currentTimeMillis() - startTime;
      if (requestTime > 300) {
        if (channel != null) {
          logger.warning("Request for channel " + channel.getName() + 
              " (" + channel.getMembers().size() + " members) took " +
              requestTime + "ms");
        } else {
          logger.warning("Request took " + requestTime + "ms");
        }
      }
    } finally {    
      datastore.endRequest();
    }
  }

  private static void handleEcho(Message xmppMessage) {
    logger.severe("Body of message sent to echo@ is: " + xmppMessage.getBody());
    SendUtil.sendMessage(
        "echo: " + xmppMessage.getBody(),
        xmppMessage.getRecipientJids()[0],
        Collections.singletonList(xmppMessage.getFromJid()));
  }

}
