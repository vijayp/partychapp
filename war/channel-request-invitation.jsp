<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="com.imjasonh.partychapp.Channel"%>
<%@ page import="com.imjasonh.partychapp.Datastore"%>
<%@ page import="com.imjasonh.partychapp.Member"%>

<!DOCTYPE html>
<html>
<head>
<%
	UserService userService = UserServiceFactory.getUserService();
	User user = userService.getCurrentUser();
  Channel channel = (Channel) request.getAttribute("channel");
  Datastore datastore = Datastore.instance();
  boolean alreadyRequestedInvitation =
      channel.hasRequestedInvitation(user.getEmail());
%>
<jsp:include page="include/head.jsp">
  <jsp:param name="subtitle" value="<%=&quot;Room &quot; + channel.getName()%>"/>
</jsp:include>
</head>
<body>
  <jsp:include page="include/header.jsp">
    <jsp:param name="subtitle" value="<%=channel.getName()%>"/>
  </jsp:include>

  <p>
    The chat room <b><%=channel.getName()%></b> is <b>invitation-only</b>. If
    you'd like to join, you can request an invitation below.
  </p>

  <p>
    <% if (!alreadyRequestedInvitation) { %>
      <button onclick='requestInvitation("<%=channel.getName()%>");'><b>Request Invitation</b></button>
    <% } else { %>
      <span style="color: #555">
        You've requested an invitation. If the current members invite you, you'll
        get a chat request and an email.
      </span>
    <% } %>
  </p>

  <jsp:include page="include/footer.jsp"/>
</body>
</html>
