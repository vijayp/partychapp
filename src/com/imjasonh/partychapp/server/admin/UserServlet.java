package com.imjasonh.partychapp.server.admin;

import com.google.appengine.api.xmpp.JID;
import com.google.common.base.Strings;

import com.imjasonh.partychapp.CachingDatastore;
import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.User;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Dumps basic information about a user.
 * 
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class UserServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    if (Strings.isNullOrEmpty(req.getPathInfo())) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    
    // Strip leading slash to get user name
    String userName = req.getPathInfo().substring(1);
    
    Datastore datastore = Datastore.instance();
    datastore.startRequest();
    try {
      User user = datastore.getUserByJID(userName);
      
      if (user == null) {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
      }
      
      resp.setContentType("text/plain; charset=utf-8");
      
      Writer writer = resp.getWriter();
      
      writer.write("JID: " + user.getJID() + "\n");
      if (datastore instanceof CachingDatastore) {
        CachingDatastore cachingDatastore = (CachingDatastore) datastore;
        writer.write("Cache key: " + cachingDatastore.getKey(user) + "\n");
      }      
      writer.write("Email: " + user.getEmail() + "\n");
      writer.write("Channels:\n");
      for (Channel channel : user.getChannels()) {
        Member member = channel.getMemberByJID(new JID(user.getJID()));
        writer.write("  " + channel.getName() + " as " + (member != null ? member.getAlias() : "N/A") + "\n");
      }
    } finally {
      datastore.endRequest();
    }
  }
}
