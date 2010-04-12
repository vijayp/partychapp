<!-- Common markup, meant to be included at the start of the <body> section -->

<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>

<div id="main"> <!-- closed in footer.jsp -->
  <div id="loginlogout" style="text-align: right">
    <%
      UserService userService = UserServiceFactory.getUserService();
      User user = userService.getCurrentUser();

      if (user != null) {
    %> <a href="<%=userService.createLogoutURL(request.getRequestURI())%>">sign
    out of <%=user.getEmail()%></a> <%
      } else {
     %> <a href="<%=userService.createLoginURL(request.getRequestURI())%>">sign
    in</a> <%
      }
     %>
  </div>
  <div id="header">
    <img src="/images/logo.png" width="310" height="150" alt="Partychat">
  </div>