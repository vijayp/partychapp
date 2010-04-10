package com.imjasonh.partychapp.server.json;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import com.imjasonh.partychapp.Datastore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class JsonServlet extends HttpServlet {
  private static final Logger logger =
      Logger.getLogger(JsonServlet.class.getName());

  public static final long serialVersionUID = 4324234259835098L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();
    if (user == null) {
      try {
        resp.getWriter().write(
            new JSONObject().put("error", "not logged in").toString());
      } catch (JSONException err) {
        logger.log(Level.WARNING, "Could not output JSON", err);
      }
      return;
    }


    Datastore datastore = Datastore.instance();
    datastore.startRequest();
    com.imjasonh.partychapp.User pchappUser =
        datastore.getUserByJID(user.getEmail());
    if (pchappUser == null) {
      try {
        resp.getWriter().write(
            new JSONObject().put("error", "you don't exist").toString());
      } catch (JSONException err) {
        logger.log(Level.WARNING, "Could not output JSON", err);
      }
      datastore.endRequest();
      return;
    }
    
    try {
      resp.getWriter()
          .write(getJson(req, resp, pchappUser, datastore).toString());
    } catch (JSONException err) {
      logger.log(Level.WARNING, "Could not output JSON", err);
    }

    datastore.endRequest();
  }


  protected abstract JSONObject getJson(
      HttpServletRequest req,
      HttpServletResponse resp, 
      com.imjasonh.partychapp.User pchappUser,
      Datastore datastore) throws JSONException;
}
