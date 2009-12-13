package com.imjasonh.partychapp.server.json;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.imjasonh.partychapp.Datastore;

public class JsonServlet  extends HttpServlet {
	  @SuppressWarnings("unused")
	  private static final Logger LOG = Logger.getLogger(JsonServlet.class.getName());
	 	  
	  @Override
	  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	      throws ServletException, IOException {
		  	    UserService userService = UserServiceFactory.getUserService();
	    User user = userService.getCurrentUser();
	    if (user == null) {
		    try {
				resp.getWriter().write(
						new JSONObject().put("error", "not logged in").toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		    return;
	    }
	    
	    
	    Datastore datastore = Datastore.instance();
	    datastore.startRequest();
		com.imjasonh.partychapp.User pchappUser = datastore.getUserByJID(user.getEmail());
		if (pchappUser == null) {
		    try {
				resp.getWriter().write(
						new JSONObject().put("error", "you don't exist").toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		    return;
			
		}
	    try {
			resp.getWriter().write(getJson(req, resp, pchappUser, datastore).toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	    datastore.endRequest();
	  }


	protected JSONObject getJson(HttpServletRequest req, HttpServletResponse resp,
			com.imjasonh.partychapp.User pchappUser, Datastore datastore) throws JSONException, IOException {
		return null;
	}
}
