package com.imjasonh.partychapp.server.web;

import java.io.IOException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.ppb.Target;

public class ChannelServlet extends HttpServlet {
  public static final long serialVersionUID = 985749740983755L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();

    String[] paths = req.getRequestURI().split("/");
    String channelName = paths[paths.length - 1];

    Datastore datastore = Datastore.instance();
    datastore.startRequest();
    try {
      Channel channel =
          datastore.getChannelIfUserPresent(channelName, user.getEmail());
      if (channel != null) {
        RequestDispatcher disp;
        disp = getServletContext().getRequestDispatcher("/channel.jsp");
        JSONArray targetsJson = new JSONArray();
        try {
          List<Target> targets = datastore.getTargetsByChannel(channel);
          for (Target t : targets) {
            JSONObject target = new JSONObject();
            target.put("name", t.name());
            target.put("score", t.score());
            targetsJson.put(target);
          }
        } catch (JSONException e) {
          throw new RuntimeException(e);
        }
        req.setAttribute("targetInfo", targetsJson.toString());
        req.setAttribute("channel", channel);
        disp.forward(req, resp);
      } else {
        resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        resp.getWriter().write("Access denied");
      }
    } finally {
      datastore.endRequest();
    }
  }
}
