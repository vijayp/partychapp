<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ page import="com.imjasonh.partychapp.Channel"%>
<%@ page import="com.imjasonh.partychapp.Datastore"%>
<%@ page import="com.imjasonh.partychapp.Member"%>

<!DOCTYPE html>
<html>
<head>
<%
  Channel channel = (Channel) request.getAttribute("channel");
  Datastore datastore = Datastore.instance();
%>
<jsp:include page="include/head.jsp">
  <jsp:param name="subtitle" value="<%="Room " + channel.getName()%>"/>
</jsp:include>
</head>
<body>
  <jsp:include page="include/header.jsp">
    <jsp:param name="subtitle" value="<%=channel.getName()%>"/>
  </jsp:include>

<p>
  You've been invited to join the room  <b><%=channel.getName()%></b>. You
  might recognize some of the other members (listed below).
</p>

<p>
  <button onclick='acceptInvitation("<%=channel.getName()%>");'><b>Accept Invitation</b></button>
  <button onclick='declineInvitation("<%=channel.getName()%>");'>Decline Invitation</button>
</p>

<jsp:include page="include/channel-members.jsp"/>

<jsp:include page="include/footer.jsp"/>
</body>
</html>
