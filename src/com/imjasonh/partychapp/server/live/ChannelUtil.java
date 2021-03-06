package com.imjasonh.partychapp.server.live;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.Message;
import com.google.appengine.api.xmpp.MessageBuilder;
import com.google.appengine.api.xmpp.SendResponse;
import com.google.appengine.api.xmpp.XMPPService;
import com.google.appengine.api.xmpp.XMPPServiceFactory;
import com.google.common.collect.ImmutableMap;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Member;

import org.json.JSONObject;


import java.util.Date;

/**
 * Utility code related to the App Engine Channel API. 
 *
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class ChannelUtil {
  private static final long MAX_LIVE_PING_INTERVAL = 10 * 60 * 1000L; 
  
  public static String getClientId(Channel channel, Member member) {
    return member.getJID().toLowerCase() + "@" + channel.getName();
  }
  
  public static void sendMessage(Channel channel, Member member, String message) {
    Date now = new Date();
    Date lastLivePing = member.getLastLivePing();
    if (lastLivePing == null ||
        now.getTime() - lastLivePing.getTime() > MAX_LIVE_PING_INTERVAL) {
      return;
    }
    String clientId = getClientId(channel, member);
    sendRawMessage(message, clientId);
  }

	public static void sendRawMessage(String message, String clientId) {
		ChannelService channelService = ChannelServiceFactory.getChannelService();
    String messageJson = new JSONObject(ImmutableMap.of("message", message)).toString();
    ChannelMessage channelMessage = new ChannelMessage(clientId, messageJson);
    channelService.sendMessage(channelMessage);
	}
	
	public static boolean sendMessage(String message, String jidString,
				String fromJid){
    JID jid = new JID(jidString);
    Message msg = new MessageBuilder()
        .withRecipientJids(jid)
        .withFromJid(new JID(fromJid))
        .withBody(message)
        .build();
    XMPPService xmpp = XMPPServiceFactory.getXMPPService();
//    xmpp.sendInvitation(new JID(email), serverJID);
    
    SendResponse status = xmpp.sendMessage(msg);
    return (status.getStatusMap().get(jid) == SendResponse.Status.SUCCESS);
	}
}
