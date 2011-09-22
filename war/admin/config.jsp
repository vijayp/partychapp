<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ page import="com.imjasonh.partychapp.Configuration"%>
<%@ page import="com.imjasonh.partychapp.PersistentConfiguration"%>

<%
  PersistentConfiguration persistentConfig = Configuration.persistentConfig();
%>

<!DOCTYPE html>
<html>
<head>
  <title>Partychapp Admin UI: Configuration</title>
</head>
<body>

<h2>Current configuration</h2>

<p>
  <b>Session token:</b> <%=persistentConfig.sessionToken()%><br>
  <b>List feed URL:</b> <%=persistentConfig.listFeedUrl()%><br>
  <b>Channel stats enabled:</b> <%=Boolean.toString(persistentConfig.areChannelStatsEnabled())%><br>
  <b>Embedly key:</b> <%=persistentConfig.embedlyKey()%>
</p>

<p>
  <!-- Use a JavaScript date so that the timezone is the current user's -->
  Last loaded at <b><script>document.write(new Date(<%=Configuration.getPeristentConfigLoadTimeMillis()%>));</script></b>.
</p>

<p><a href="/admin/config/edit">Edit</a></p>

<p><a href="/admin/config/reload">Reload</a> (refresh a few times to hit all instances)</p>

</body>
</html>
