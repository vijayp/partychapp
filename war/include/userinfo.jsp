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
  <h3>My Channels</h3>
</div>

<script>
  function displayChannels(userInfo, targetDiv) {
    targetDiv.setAttribute('style', 'display: block');
    if (userInfo.error) {
      targetDiv.innerHTML = "ERROR: " + userInfo.error;
      return;
    }

    var channels = userInfo['channels'];
    for (var i = 0; i < channels.length; i++) {
       var channelName = channels[i].name;
       var nameDiv = document.createElement('div');
       var nameAnchor = document.createElement('a');
       nameAnchor.href = '/channel/' + channelName;
       var nameNode = document.createTextNode(channelName);
       nameAnchor.appendChild(nameNode);
       nameDiv.appendChild(nameAnchor);
       nameDiv.appendChild(document.createTextNode(' (alias: ' + channels[i].alias + ')'));
       targetDiv.appendChild(nameDiv);
    }
  }

  var userInfo = <%=UserInfoJsonServlet.getJsonFromUser(pchappUser, datastore)%>;
  displayChannels(userInfo, document.getElementById('channels'));
</script>

<%
  datastore.endRequest();
%>