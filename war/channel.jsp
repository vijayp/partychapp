<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ page import="java.util.Collections"%>
<%@ page import="java.util.List"%>
<%@ page import="com.google.appengine.repackaged.com.google.common.collect.Lists"%>
<%@ page import="com.imjasonh.partychapp.Channel"%>
<%@ page import="com.imjasonh.partychapp.Datastore"%>
<%@ page import="com.imjasonh.partychapp.Member"%>
<%@ page import="com.imjasonh.partychapp.ppb.Reason"%>
<%@ page import="com.imjasonh.partychapp.ppb.Target"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%
	Channel channel = (Channel) request.getAttribute("channel");
	Datastore datastore = Datastore.instance();
%>
<html>
<head>
<meta http-equiv="content-type" content="text/html; charset=UTF-8">
<link type="text/css" rel="stylesheet" href="/Partychapp.css">
<title>Partychapp - Channel Stats: <%=channel.getName()%></title>
</head>
<body>
  <div id="main">
     <div id="header">
      <img src="/logo.png" width="310" height="150" alt="Partychat">
    </div>

<div class="channelName">Channel stats for: 
<span style="font-weight:bold"><%=channel.getName()%></span></div>

<script>
function addReasons(cell, name, score) {
}
</script>

<div class="channelHeading">PlusPlusBot</div>
<%
		List<Target> targets = datastore.getTargetsByChannel(channel);
		for (Target t : targets) {
	%>
<div>
<table>
	<tr>
		<td width="500" onclick="addReasons(this, '<%=t.name()%>', '<%=t.score()%>')">
		<%=t.name()%></td>
		<td><%=t.score()%></td>
	</tr>
</table>
<% } %>
<P></P>
<div class="channelHeading">Members</div>
<table class="scoreTable">
	<tr>
		<td>Alias</td>
		<td>Email address</td>
	</tr>
	<%
	   List<Member> members = Lists.newArrayList(channel.getMembers());
    Collections.sort(members, new Member.SortMembersForListComparator());
    for (Member m : members) {
    	%>
	<tr>
		<td><%=m.getAlias()%></td>
		<td><%=m.getJID()%></td>
	</tr>
	<% } %>
</table>
</div></body>
</html>