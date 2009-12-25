<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ page import="java.util.Collections"%>
<%@ page import="java.util.List"%>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="com.google.appengine.repackaged.com.google.common.collect.Lists"%>
<%@ page import="com.imjasonh.partychapp.Channel"%>
<%@ page import="com.imjasonh.partychapp.Datastore"%>
<%@ page import="com.imjasonh.partychapp.Member"%>
<%@ page import="com.imjasonh.partychapp.ppb.Reason"%>
<%@ page import="com.imjasonh.partychapp.ppb.Target"%>
<%@ page import="com.imjasonh.partychapp.server.json.UserInfoJsonServlet"%>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<%
UserService userService = UserServiceFactory.getUserService();
User user = userService.getCurrentUser();
Datastore datastore = Datastore.instance();
datastore.startRequest();
com.imjasonh.partychapp.User pchappUser = datastore.getUserByJID(user.getEmail());
%>

<meta http-equiv="content-type" content="text/html; charset=UTF-8">
<script type="text/javascript">var _sf_startpt=(new Date()).getTime()</script>
<link type="text/css" rel="stylesheet" href="/Partychapp.css">
<title>Partychapp - User Info</title>
</head>
<body>
<%
if (user == null) {
%>
<script>
location.href="<%=userService.createLoginURL(request.getRequestURI()) %>";
</script>
<% } %>

<div id="main">
<div id="header"><img src="/logo.png" width="310" height="150"
	alt="Partychat"></div>
<P></P>
<div id="channels" style="display: none">Channels:
<div id="data"></div>

<script>

function displayInfo(userInfo) {
    if (userInfo.error) {
        var headerDiv = document.getElementById("header");
        var header = document.createElement("div");
        header.setAttribute('style', "text-align: center; font-size: 150%");
        header.innerHTML = "ERROR: " + userInfo.error;
        headerDiv.appendChild(header);
        return;
    }

    var headerDiv = document.getElementById("header");
    var header = document.createElement("div");
    header.setAttribute('style', "text-align: center; font-size: 150%");
    header.innerHTML = "Stats for: " + userInfo['email'];
    headerDiv.appendChild(header);

    document.getElementById("channels").setAttribute('style', 'display: block');
    var dataDiv = document.getElementById("data");
    var channels = userInfo['channels'];
    for (var i = 0; i < channels.length; i++) {
       var channelName = channels[i].name;     
       var nameDiv = document.createElement("div");
       var nameAnchor = document.createElement("a");
       nameAnchor.href = "/channel/" + channelName;
       var nameNode = document.createTextNode(channelName);
       nameAnchor.appendChild(nameNode);
       nameDiv.appendChild(nameAnchor);
       nameDiv.appendChild(document.createTextNode(" (alias: " + channels[i].alias + ")"));
       dataDiv.appendChild(nameDiv);
    }
}

var userInfo = <%=UserInfoJsonServlet.getJsonFromUser(pchappUser, datastore)%>
displayInfo(userInfo);

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

</div>
</div>
</body>
</html>