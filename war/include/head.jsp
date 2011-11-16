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
<tr><td>
<h3>Migration update:</h3>
<p>

If your channel has been migrated, and you cannot access it from a Google 
Apps-controlled domain (i.e. not user@gmail.com), here is <a href="http://www.vijayp.ca/blog/?p=152">a description of how to get partychat working with Google Apps domains.</a>
</p>

<p><a href="/migration.html">Migration to a new server</a> is currently under way. 
</p><p>
Please follow <a href="http://www.twitter.com/partychat">partychat on Twitter</a> for more up to date details.
</p>


</b></td></tr>
</table>
</center>