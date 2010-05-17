package com.imjasonh.partychapp.server.admin;

import com.google.common.base.Strings;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.User;
import com.imjasonh.partychapp.server.json.UserInfoJsonServlet;

import org.json.JSONException;

import java.io.IOException;

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
      
      // For now, easiest to reuse the JSON output
      try {
        resp.getWriter().write(
            UserInfoJsonServlet.getJsonFromUser(user, datastore).toString());
      } catch (JSONException err) {
        // Shouldn't happen.
        throw new RuntimeException(err);
      }      

    } finally {
      datastore.endRequest();
    }
  }
}
