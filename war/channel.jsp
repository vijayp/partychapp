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
<script type="text/javascript">var _sf_startpt=(new Date()).getTime()</script>
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
function displayReasons(cell, data) {
    var reasons = eval("(" + data + ")")['reasons'];
	var reasonsDiv = document.createElement("div");
	reasonsDiv.class = "reasons";
    for (var i = 0; i < reasons.length; ++i) {	
    	reasonsDiv.innerHTML += reasons[i].reason + " (" + reasons[i].sender + ")<BR>";
    }
	cell.appendChild(reasonsDiv);
	cell.expanded = reasonsDiv;
}

function addReasons(cell, channelName, targetName) {
	if (cell.isExpanded) {
		if (cell.expanded.style.display == 'none') {
			cell.expanded.style.display = 'block';
		} else {
			cell.expanded.style.display = 'none';
		}
		return;
	}
	cell.isExpanded = true;
	
	var url = "/reasons/" + channelName + "/" + targetName;
	var xmlHttp = new XMLHttpRequest();
	xmlHttp.open('GET', url, true);
	xmlHttp.onreadystatechange = function(){
	if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
		displayReasons(cell, xmlHttp.responseText);
    }};

    xmlHttp.send(null);
}  

</script>

<div class="channelHeading">PlusPlusBot</div>

<table>
	<tr>
		<td width="450px">Target</td>
		<td>Score</td>
	</tr>
</table>
<%
		List<Target> targets = datastore.getTargetsByChannel(channel);
		for (Target t : targets) {
	%>

<div
	onclick="addReasons(this, '<%=channel.getName()%>', '<%=t.name()%>')">
<table>
	<tr>
		<td width="450px"><%=t.name()%></td>
		<td><%=t.score()%></td>
	</tr>
</table>
</div>
<% } %>
<P></P>
<div class="channelHeading">Members</div>
<table class="scoreTable">
	<tr>
		<td width="100px">Alias</td>
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
<P></P>
<div class="channelHeading">Invited</div>
	<%
	   List<String> invitedMembers = Lists.newArrayList(channel.getInvitees());
    Collections.sort(invitedMembers);
    for (String m : invitedMembers) { %>
    	<%=m%><BR></BR>
	<% } %>
<P></P>
Invite People!
<div id="invite" style="border: 1px solid #ccc">
<table cellpadding=10>
	<tr>
		<td>
		<form action="/invite" method="post" target="inviteResults">
		<input type="hidden" name="name" value="<%=channel.getName()%>"/>
		Email addresses you would like to invite? (separated by commas)<br>
		<textarea name="invitees"></textarea> <br>
		<br>
		<input type="submit" value="Invite!"></form>
		</td>
		<td><iframe frameborder=0 name="inviteResults"> </iframe></td>
	</tr>
</table>
</div>
</div>
<script type="text/javascript">
var _sf_async_config={uid:2197,domain:"partychapp.appspot.com"};
(function(){
  function loadChartbeat() {
    window._sf_endpt=(new Date()).getTime();
    var e = document.createElement('script');
    e.setAttribute('language', 'javascript');
    e.setAttribute('type', 'text/javascript');
    e.setAttribute('src',
       (("https:" == document.location.protocol) ? "https://s3.amazonaws.com/" : "http://") +
       "static.chartbeat.com/js/chartbeat.js");
    document.body.appendChild(e);
  }
  var oldonload = window.onload;
  window.onload = (typeof window.onload != 'function') ?
     loadChartbeat : function() { oldonload(); loadChartbeat(); };
})();

</script>
</body>
</html>