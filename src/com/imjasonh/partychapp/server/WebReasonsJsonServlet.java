package com.imjasonh.partychapp.server;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mortbay.util.ajax.JSON;

import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.repackaged.com.google.common.base.Pair;
import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.ppb.Reason;
import com.imjasonh.partychapp.ppb.Target;

// TODO(blackmad): always use the same error message

public class WebReasonsJsonServlet  extends HttpServlet {
	  @SuppressWarnings("unused")
	  private static final Logger LOG = Logger.getLogger(WebServlet.class.getName());
	  
	  @Override
	  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	      throws ServletException, IOException {
		  // can I please
	    UserService userService = UserServiceFactory.getUserService();
	    User user = userService.getCurrentUser();
	    if (user == null) {
	    	return;
	    }
	       
	    String[] paths = req.getRequestURI().split("/");
	    if (paths.length < 3) {
	    	return;
	    }
	    String channelName = paths[paths.length - 2];
	    String targetName = paths[paths.length - 1];
	    
	    Datastore datastore = Datastore.instance();
	    datastore.startRequest();   
	    Channel channel = datastore.getChannelIfUserPresent(channelName, user.getEmail());
	    if (channel == null) {
	    	return;
	    }
	    
	    JSONArray list = new JSONArray();
	    Target t = datastore.getTarget(channel, targetName);
	    for (Reason r: datastore.getReasons(t, 100)) {
		    JSONObject reasonJson = new JSONObject();
		    try {
		    	reasonJson.put("reason", r.reason());
		    	reasonJson.put("sender", r.sender().getAlias());
		    	list.put(reasonJson);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    resp.getWriter().write(list.toString());
	  }
}
