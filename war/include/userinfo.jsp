<!-- Displays which channels the current user is in -->

<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="com.imjasonh.partychapp.Datastore"%>
<%@ page import="com.imjasonh.partychapp.server.json.UserInfoJsonServlet"%>

<%
	UserService userService = UserServiceFactory.getUserService();
	User user = userService.getCurrentUser();
  Datastore datastore = Datastore.instance();
  datastore.startRequest();
  com.imjasonh.partychapp.User pchappUser = datastore.getOrCreateUser(user.getEmail());
%>

<div id="channels" style="display: none">
  <h3>My Rooms</h3>
</div>

<script>
  var userInfo = <%=UserInfoJsonServlet.getJsonFromUser(pchappUser, datastore)%>;
  displayChannels(userInfo, document.getElementById('channels'));
</script>

<%
  datastore.endRequest();
%>