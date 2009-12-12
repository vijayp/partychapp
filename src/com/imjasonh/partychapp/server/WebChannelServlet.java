package com.imjasonh.partychapp.server;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;

public class WebChannelServlet  extends HttpServlet {
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(WebServlet.class.getName());
	
	public static final long serialVersionUID = 985749740983755L;

	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();
		if (user == null) {
			resp.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
			resp.setHeader("Location", 
					userService.createLoginURL(req.getRequestURL().toString()));  
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
