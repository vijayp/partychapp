<!-- Common markup, meant to be included in the <head> section -->

<%@ page import="com.google.common.base.Strings"%>

<meta http-equiv="content-type" content="text/html; charset=UTF-8">
<title>
  Partychat
  <% if (!Strings.isNullOrEmpty(request.getParameter("subtitle"))) { %>
  -
  <%=request.getParameter("subtitle")%>
  <% } %>
</title>
<script type="text/javascript">
  var _sf_startpt=(new Date()).getTime();
</script>
<link type="text/css" rel="stylesheet" href="/main.css">
<script type="text/javascript" src="/closure-lite.js"></script>
<script type="text/javascript" src="/main.js"></script>
