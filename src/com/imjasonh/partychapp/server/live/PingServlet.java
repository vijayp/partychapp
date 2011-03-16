package com.imjasonh.partychapp.server.live;

import com.google.appengine.api.users.User;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.server.web.AbstractChannelUserServlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Updates the last seen timestamp for a member using the live UI, so that we'll
 * keep delivering messages to them ({@link ChannelUtil} will not attempt to
 * deliver messages to users that have not been seen for a while).
 *
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class PingServlet extends AbstractChannelUserServlet {
  @Override protected void doChannelGet(
      HttpServletRequest req,
      HttpServletResponse resp,
      User user,
      Channel channel)  {
    Member member = channel.getMemberByJID(user.getEmail());
    member.onLivePing();
    channel.put();
    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
  }
}
