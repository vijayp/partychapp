<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="com.imjasonh.partychapp.Configuration" %>
<%@ page import="com.imjasonh.partychapp.Datastore"%>
<%@ page import="com.imjasonh.partychapp.server.HttpUtil"%>

<%
	UserService userService = UserServiceFactory.getUserService();
	User user = userService.getCurrentUser();
	final boolean logged_in = null != user;
	
%>

<!DOCTYPE html>
<html>
<head>
  <jsp:include page="include/head.jsp"/>
</head>
<body>
<jsp:include page="include/header.jsp"/>
<a name="donate"><h3>How can I donate?</h3></a>
<jsp:include page="include/donate.jsp" />

<jsp:include page="include/footer.jsp"/>
</body>
</html>