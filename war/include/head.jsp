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
<script type="text/javascript" src="http://dev.jquery.com/view/trunk/plugins/validate/jquery.validate.js"></script>
<link type="text/css" rel="stylesheet" href="/main.css">
<script type="text/javascript" src="/partychapp_compiled.js"></script>

<center><table BORDER="1px" style="background-color: yellow;" width=800px>
<tr><td><b>
Partychat is currently experiencing <a href="/migration.html">migration issues</a>. We expect to be back by noon on Nov 8 EST. 
Sorry for the inconvenience. Please try back tomorrow.
</b></td></tr>
</table>
</center>