package com.imjasonh.partychapp.server.web;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.repackaged.com.google.common.collect.Lists;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.server.SendUtil;
import com.imjasonh.partychapp.server.command.InviteHandler;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class InviteToRoomServlet extends HttpServlet {

  @SuppressWarnings("unused")
  private static final Logger LOG = Logger.getLogger(InviteToRoomServlet.class.getName());

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();
    
    if (user == null) {
    	resp.getWriter().write("not logged in");
    	return;
    }
    
    String channelName = req.getParameter("name");
    Datastore datastore = Datastore.instance();
    datastore.startRequest();
	Channel channel = datastore.getChannelIfUserPresent(channelName, user.getEmail());
	if (channel == null) {
    	resp.getWriter().write("access denied");
    	return;
	}
  
    resp.getWriter().write("<style>body { font-family: Helvetica, sans-serif }</style>");
    List<String> invitees = Lists.newArrayList();
    if (!req.getParameter("invitees").isEmpty()) {
    	String error = InviteHandler.parseEmailAddresses(req.getParameter("invitees"), invitees);
    	for (String invitee : invitees) {
    		channel.invite(invitee);
    		SendUtil.invite(invitee, channel.serverJID());
    		error += "Invited " + invitee + "<br>";
    	}
	    resp.getWriter().write(error);
    } else {
    	resp.getWriter().write("No one to invite.<P>");
    }

    channel.put();
    datastore.endRequest();    
  }
}
