package com.imjasonh.partychapp.server.live;

import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.users.User;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.server.web.AbstractChannelUserServlet;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Displays the landing page for the live channel UI. 
 *
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class ChannelServlet extends AbstractChannelUserServlet {
  @Override protected String getChannelName(HttpServletRequest req) {
    return req.getPathInfo().substring(1);
  }
  
  @Override protected void doChannelGet(
      HttpServletRequest req,
      HttpServletResponse resp,
      User user,
      Channel channel)
      throws IOException, ServletException {
    Member member = channel.getMemberByJID(user.getEmail());
    member.onLivePing();
    channel.put();
    
    String clientId = ChannelUtil.getClientId(channel, member);
    
    ChannelService channelService = ChannelServiceFactory.getChannelService();
    String token = channelService.createChannel(clientId);
  
    req.setAttribute("channel", channel);
    req.setAttribute("token", token);
    RequestDispatcher disp =
      getServletContext().getRequestDispatcher("/channel-live.jsp");
    disp.forward(req, resp);
  }
 }
