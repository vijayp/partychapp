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
<html>
<head>

<script>

function displayInfo(data) {
    var userInfo = eval("(" + data + ")");

    var headerDiv = document.getElementById("header");
    var header = document.createElement("div");
    header.setAttribute('style', "text-align: center; font-size: 150%");
    header.innerHTML = "Stats for: " + userInfo['email'];
    headerDiv.appendChild(header);

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

function processInfo() {
  var url = "/userinfo";

  var xmlHttp = new XMLHttpRequest();
  xmlHttp.open('GET', url, true);
  xmlHttp.onreadystatechange = function(){
      if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
		displayInfo(xmlHttp.responseText);
      }};

    xmlHttp.send(null);
}  
</script>

<meta http-equiv="content-type" content="text/html; charset=UTF-8">
<link type="text/css" rel="stylesheet" href="/Partychapp.css">
<title>Partychapp - User Info</title>
</head>
<body onLoad="processInfo();">
  <div id="main">
     <div id="header">
      <img src="/logo.png" width="310" height="150" alt="Partychat">
    </div>
    <P></P>
    Channels:
	<div id="data"></div>
</div>
</body>
</html>