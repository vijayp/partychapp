package com.imjasonh.partychapp.server.web;

import com.google.appengine.api.users.User;

import com.imjasonh.partychapp.Channel;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Allows invite-only and logging to be turned on/off for a channel (by a member
 * of that channel). 
 *
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class ChannelEditServlet extends AbstractChannelUserServlet {
  @Override
  protected void doChannelPost(
      HttpServletRequest req,
      HttpServletResponse resp,
      User user,
      Channel channel)
      throws IOException {
    channel.setInviteOnly(
        Boolean.parseBoolean(req.getParameter("inviteonly")));
    channel.setLoggingDisabled(
        !Boolean.parseBoolean(req.getParameter("logging")));
    
    channel.put();
    
    resp.sendRedirect(channel.webUrl());
  }
}
