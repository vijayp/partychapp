package com.imjasonh.partychapp.server.live;

import com.google.appengine.api.users.User;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.server.web.AbstractChannelUserServlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Marks a channel member as not using live UI, so that we don't try to send
 * any messages to them on that channel. 
 *
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class LeaveServlet extends AbstractChannelUserServlet {
  @Override protected void doChannelGet(
      HttpServletRequest req,
      HttpServletResponse resp,
      User user,
      Channel channel)  {
    Member member = channel.getMemberByJID(user.getEmail());
    member.clearLivePing();
    channel.put();
    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
  }
}
