<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ page import="com.imjasonh.partychapp.Channel"%>
<%@ page import="com.imjasonh.partychapp.Datastore"%>
<%@ page import="com.imjasonh.partychapp.Member"%>
<%@ page import="com.imjasonh.partychapp.ppb.Reason"%>
<%@ page import="com.imjasonh.partychapp.ppb.Target"%>

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
  You're in the room <b><%=channel.getName()%></b> along with the people below.
  You can  <a href="#invite-section">invite more people</a> or
  <a href="/channel/leave?name=<%=channel.getName()%>">leave</a> the room.
</p>

<jsp:include page="include/channel-members.jsp"/>

<h3>PlusPlusBot</h3>

<div id="score-table"></div>

<h3 id="invite-section">Invite People!</h3>

<table>
  <tr>
    <td>
    <form action="/channel/invite" method="post" target="inviteResults">
    <input type="hidden" name="name" value="<%=channel.getName()%>"/>
    Email addresses you would like to invite? (separated by commas)<br>
    <textarea name="invitees" rows="4" cols="40"></textarea> <br>
    <br>
    <input type="submit" value="Invite!"></form>
    </td>
    <td><iframe frameborder=0 name="inviteResults"> </iframe></td>
  </tr>
</table>
<script>
  new partychapp.ScoreTable('<%= channel.getName() %>',
                            <%= (String) request.getAttribute("targetInfo") %>);
</script>

<jsp:include page="include/footer.jsp"/>
</body>
</html>
