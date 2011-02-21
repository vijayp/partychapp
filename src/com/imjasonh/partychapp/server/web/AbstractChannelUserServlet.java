package com.imjasonh.partychapp.server.web;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Base class for servlets that need to operate on a channel that the requesting
 * user is currently in (the channel name is specified via the "name"
 * parameter). Handles authentication and verification. Subclasses should
 * implement {@link #doChannelGet} and/or {@link #doChannelPost}.
 *
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public abstract class AbstractChannelUserServlet extends HttpServlet {
  private interface MethodAdapter {
    void invokeMethod(
        HttpServletRequest req,
        HttpServletResponse resp,
        User user,
        Channel channel)
        throws IOException;
  }
  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    doMethod(req, resp, new MethodAdapter() {
      @Override public void invokeMethod(
          HttpServletRequest req,
          HttpServletResponse resp,
          User user,
          Channel channel) throws IOException {
        doChannelGet(req, resp, user, channel);
      }
    });
  }
  
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    doMethod(req, resp, new MethodAdapter() {
      @Override public void invokeMethod(
          HttpServletRequest req,
          HttpServletResponse resp,
          User user,
          Channel channel) throws IOException {
        doChannelPost(req, resp, user, channel);
      }
    });
  }  
  
  private void doMethod(HttpServletRequest req, HttpServletResponse resp, MethodAdapter methodAdapter)
      throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();
    
    String channelName = req.getParameter("name");
    Datastore datastore = Datastore.instance();
    try {
      datastore.startRequest();
      Channel channel =
          datastore.getChannelIfUserPresent(channelName, user.getEmail());
      if (channel == null) {
        resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        return;
      }
      
      methodAdapter.invokeMethod(req, resp, user, channel);
    } finally {
      datastore.endRequest();
    }        
  }
  
  protected void doChannelGet(
      HttpServletRequest req,
      HttpServletResponse resp,
      User user,
      Channel channel)
      throws IOException {
    resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
  }
  
  protected void doChannelPost(
      HttpServletRequest req,
      HttpServletResponse resp,
      User user,
      Channel channel)
      throws IOException {
    resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
  }  
}
