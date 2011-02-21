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
public class InviteToChannelServlet extends AbstractChannelUserServlet {

  @SuppressWarnings("unused")
  private static final Logger logger =
      Logger.getLogger(InviteToChannelServlet.class.getName());

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
    if (!req.getParameter("invitees").isEmpty()) {
      String error = InviteHandler.parseEmailAddresses(
          req.getParameter("invitees"), invitees);
      for (String invitee : invitees) {
        channel.invite(invitee);
        String inviteError = InviteUtil.invite(
            invitee,
            channel,
            user.getEmail(),
            user.getEmail()); 
        if (Strings.isNullOrEmpty(inviteError)) {
          error += "Invited " + invitee + "<br>";
        } else {
          error += inviteError + "<br>";            
        }

        channel.broadcastIncludingSender(
            "_" + user.getEmail() +
            " invited " + invitee + " (via the web UI)_");        
      }
      resp.getWriter().write(error);
      
      channel.put();
    } else {
      resp.getWriter().write("No one to invite.<P>");
    }
  }
}
