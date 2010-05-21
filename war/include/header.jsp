<!-- Common markup, meant to be included at the start of the <body> section -->

<%@ page import="com.google.common.base.Strings"%>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="com.imjasonh.partychapp.server.HttpUtil"%>

<div id="main"> <!-- closed in footer.jsp -->
  <div id="loginlogout" style="text-align: right">
    <%
      UserService userService = UserServiceFactory.getUserService();
      User user = userService.getCurrentUser();

      if (user != null) {
    %> <a href="<%=userService.createLogoutURL(HttpUtil.getRequestUri(request))%>">sign
    out of <%=user.getEmail()%></a> <%
      } else {
     %> <a href="<%=userService.createLoginURL(HttpUtil.getRequestUri(request))%>">sign
    in</a> <%
      }
     %>
  </div>
  <% if (Strings.isNullOrEmpty(request.getParameter("subtitle"))) { %>
    <div id="header">
      <a href="/"><img src="/images/logo.png" width="310" height="150" alt="Partychat" border="0"></a>
    </div>
  <% } else { %>
    <div id="subtitle-header">
      <a href="/"><img src="/images/logo.png" width="103" height="50" alt="Partychat" border="0"></a>
      <div id="subtitle"><%=request.getParameter("subtitle")%></div>
   </div>
 <% } %>
