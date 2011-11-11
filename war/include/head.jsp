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
<p>Due to <a href="/migration.html">issues with our migration process</a>, 
we have <b>temporarily reactivated chats on the old partychapp.appspot.com domain</b>	
for channels with fewer than 100 people until those issues are resolved. 
</p><p>
Larger channels won't be supported until these issues are resolved. 
Please follow <a href="http://www.twitter.com/partychat">partychat on Twitter</a> for more up to date details.
</p><p>
Sorry for the inconvenience
</p> 


</b></td></tr>
</table>
</center>