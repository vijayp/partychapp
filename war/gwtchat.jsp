<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.google.appengine.api.channel.ChannelService" %>
<%@ page import="com.google.appengine.api.channel.ChannelServiceFactory" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>

<html>
  <head>

<%
    UserService userService = UserServiceFactory.getUserService();
    if (userService.isUserLoggedIn()) {
        User user = userService.getCurrentUser();
		ChannelService cs = ChannelServiceFactory.getChannelService();
		String roomName = request.getParameter("channel");
		
		if (roomName == null || !roomName.equals("dogfood")) {
		  response.sendRedirect("/");
		}
%>
  <script type="text/javascript" language="javascript" src="/_ah/channel/jsapi"></script>

  <script type="text/javascript" src="partychapp/partychapp.nocache.js"></script>
  <script type="text/javascript">
    var info = { "channel" : "<%= cs.createChannel("webchat-" + roomName + "-" + user.getUserId()) %>" };
  </script>
<%
    }
%>

  </head><body>
  
<%
    if (userService.isUserLoggedIn()) {
%>
  <h3><a href="<%= userService.createLogoutURL(request.getRequestURI()) %>">Log out</a></h3>
<%
    } else {
%>
  <h3>You must <a href="<%= userService.createLoginURL(request.getRequestURI()) %>">log in</a> to use webchat</h3>
<%
    }
%>

    <iframe src="javascript:''" id="__gwt_historyFrame" tabIndex='-1' style="position:absolute;width:0;height:0;border:0"></iframe>
  </body>
</html>
