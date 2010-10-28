package com.imjasonh.partychapp.server.admin;


import com.google.appengine.api.xmpp.JID;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.User;
import com.imjasonh.partychapp.Message.MessageType;
import com.imjasonh.partychapp.server.command.Command;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Simple endpoint for doing concurrency testing. Meant to be used with a script
 * like:
 * 
 * <pre>
 * #!/bin/bash
 * for i in {1..50}
 * do
 *  curl -b "dev_appserver_login=test@example.com:true:18580476422013912411" \
 *      "http://localhost:8888/admin/concurrency-test?channel=benchmark-$i&user=user$i@example.com"&
 * done
 * wait
 * </pre>
 *
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class ConcurrencyTestServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    Datastore datastore = Datastore.instance();
    String channelName = req.getParameter("channel");
    JID userJid = new JID(req.getParameter("user"));
    JID serverJid = new JID(channelName + "@example.com");
    
    datastore.startRequest();
    
    Channel channel = datastore.getChannelByName(channelName);
    Member member = null; 
    if (channel != null) {
      member = channel.getMemberByJID(userJid);
    }
    User user = datastore.getOrCreateUser(userJid.getId().split("/")[0]);
    
    com.imjasonh.partychapp.Message message =
      new com.imjasonh.partychapp.Message.Builder()
        .setContent("hello world")
        .setUserJID(userJid)
        .setServerJID(serverJid)
        .setChannel(channel)
        .setMember(member)
        .setUser(user)
        .setMessageType(MessageType.XMPP)
        .build();

    Command.getCommandHandler(message).doCommand(message);
    
    datastore.endRequest();
    
    resp.getWriter().append("OK\n");
  }
}
