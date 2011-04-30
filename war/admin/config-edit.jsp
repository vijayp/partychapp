<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ page import="com.imjasonh.partychapp.Configuration"%>
<%@ page import="com.imjasonh.partychapp.PersistentConfiguration"%>

<%
  PersistentConfiguration persistentConfig = Configuration.persistentConfig();
%>

<!DOCTYPE html>
<html>
<head>
  <title>Partychapp Admin UI: Edit Configuration</title>
</head>
<body>

<form method="POST">
  <label>
    Session token:
    <input name="session-token" type="text" value="<%=persistentConfig.sessionToken()%>" size="50">
  </label>
  <br>
  
  <label>
    List feed URL:
    <input name="list-feed-url" type="text" value="<%=persistentConfig.listFeedUrl()%>" size="50">
  </label>
  <br>
  
  <label>
    <input type="checkbox" name="channel-stats-enabled" value="true" <% if (persistentConfig.areChannelStatsEnabled()) {out.print("checked");} %>>
    Channel stats
  </label>
  <br>
  <input type="submit">
</form>

</body>
</html>
