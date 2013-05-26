package com.imjasonh.partychapp.server.web;

import com.google.appengine.api.users.User;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.server.InviteUtil;
import com.imjasonh.partychapp.server.command.InviteHandler;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class AddGmailAccountServlet extends AbstractChannelUserServlet {

  @SuppressWarnings("unused")
  private static final Logger logger =
      Logger.getLogger(AddGmailAccountServlet.class.getName());

  @Override
  protected void doChannelPost(
      HttpServletRequest req,
      HttpServletResponse resp,
      User user,
      Channel channel)
      throws IOException {
	
    resp.getWriter().write(
        "<style>body { font-family: Helvetica, sans-serif }</style>");
    List<String> invitees = Lists.newArrayList();
    if (!req.getParameter("gmail_username").isEmpty()) {
    	if (!req.getParameter("gmail_password").isEmpty()) {
    		boolean ok = channel.setGmailUserName(req.getParameter("gmail_username")) && 
    					 channel.setGmailPassword(req.getParameter("gmail_password"));
    		if (ok) {
	    		channel.put();
	    		channel.broadcastIncludingSender("adding gmail account " + channel.getGmailUserName() + " for broadcast");
	    		resp.getWriter().write("Set username/password ok");
	    		return;
    		}
    	}
    }
    resp.getWriter().write("unknown errror, sorry.");
  }
}
