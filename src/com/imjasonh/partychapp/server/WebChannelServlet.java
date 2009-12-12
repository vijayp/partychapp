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

import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.xmpp.JID;
import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.ppb.Target;

public class WebChannelServlet  extends HttpServlet {
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(WebServlet.class.getName());

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();
		if (user == null) {
			// TODO(blackmad): redirect to a login page
			resp.getWriter().write("Sorry, not logged in.");    
			return;
		}

		String[] paths = req.getRequestURI().split("/");
		String channelName = paths[paths.length - 1];

		Datastore datastore = Datastore.instance();
		datastore.startRequest();

		Channel channel = datastore.getChannelFromWeb(user, channelName);
		if (channel != null) {
			RequestDispatcher disp;
			disp = getServletContext().getRequestDispatcher("/channel.jsp");
			req.setAttribute("channel", channel);
			disp.forward(req, resp);  
		} else {
			resp.getWriter().write("Access denied");  
			return;
		}
	}
}
