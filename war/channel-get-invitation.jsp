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
  <jsp:param name="subtitle" value="<%=&quot;Room &quot; + channel.getName()%>"/>
</jsp:include>
</head>
<body>
  <jsp:include page="include/header.jsp">
    <jsp:param name="subtitle" value="<%=channel.getName()%>"/>
  </jsp:include>

<p>
  If you'd like to join the chat room <b><%=channel.getName()%></b>, you
  can get an invitation below. You'll receive both a chat invitation and an
  email with instructions.
</p>

<p>
  <button onclick='getInvitation("<%=channel.getName()%>");'><b>Get Invitation</b></button>
</p>

<jsp:include page="include/footer.jsp"/>
</body>
</html>
