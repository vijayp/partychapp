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
import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.User;
import com.imjasonh.partychapp.Message.MessageType;
import com.imjasonh.partychapp.server.command.Command;
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
	
	public final static String PARTYCHAPP_CONTROL = "__control@partychapp.appspotchat.com";
  public final static String PARTYCHAPP_DOMAIN = "partychapp.appspotchat.com";
  public final static String PROXY_CONTROL = "_control@im.partych.at";

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

    // Various other bots that we've encountered
    Pattern.compile(".*(?:g2twit[.]appspotchat[.]com|twitalker\\d+@appspot[.]com|chitterim@appspot[.]com|tweetjid@appspot[.]com|twiyia@gmail[.]com|roomchinese[.]appspotchat[.]com|353606@gmail[.]com).*", Pattern.CASE_INSENSITIVE),
  };
    
    
  @Override
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
    
    try {
      final String fromAddr = jidToLowerCase(xmppMessage.getFromJid()).getId();
      final String toAddr   = (xmppMessage.getRecipientJids().length > 0) 
      	? jidToLowerCase(xmppMessage.getRecipientJids()[0]).getId() : "";
      
      	//logger.info("comparing <" + fromAddr + "> to <"+ PROXY_CONTROL);
      	//logger.info("comparing <" + toAddr + "> to <"+ PARTYCHAPP_CONTROL);
      	
    	if (fromAddr.startsWith(PROXY_CONTROL) &&
    			toAddr.startsWith(PARTYCHAPP_CONTROL)) {
    		logger.warning("GOT instructional packet");
    		// json decode the control packet from the body
    		// make the new message.
    		// doxmpp
    		String body = xmppMessage.getBody().trim();
    		JSONObject jso = new JSONObject(body);
    		String decodedTo = jso.getString("to_str");
    		decodedTo = decodedTo.split("@")[0] + "@" + PARTYCHAPP_DOMAIN;
    		String decodedFrom = jso.getString("from_str");
    		String decodedMsg = jso.getString("message_str");
      	Message payload = new MessageBuilder().withFromJid(new JID(decodedFrom))
      	.withBody(decodedMsg)
    		.withMessageType(com.google.appengine.api.xmpp.MessageType.CHAT)
    		.withRecipientJids(new JID(decodedTo)).build();
      	logger.info("calling doxmpp with payload :: "
      					 + payload.toString());
      	doXmpp(payload);
    	} else {
    		doXmpp(xmppMessage);
    	}
      
      resp.setStatus(HttpServletResponse.SC_OK);
    } catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
      if (QS.supports(DataType.CPU_TIME_IN_MEGACYCLES) && xmppMessage != null) {
        long endCpu = QS.getCpuTimeInMegaCycles();
        JID serverJID = jidToLowerCase(xmppMessage.getRecipientJids()[0]);
        String channelName = serverJID.getId().split("@")[0];
        ChannelStats.recordChannelCpu(channelName, endCpu - startCpu);
      }
    }
  }

  private static JID jidToLowerCase(JID in) {
    return new JID(in.getId().toLowerCase());
  }
  
  public void doXmpp(Message xmppMessage) {
  	
  	
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
  
      Command.getCommandHandler(message).doCommand(message);
      
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
