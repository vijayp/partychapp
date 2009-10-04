<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <link type="text/css" rel="stylesheet" href="Partychapp.css">
    <script type="text/javascript">
      function show(elt) {
        document.getElementById('actionOptions').style.display = 'none';
        document.getElementById(elt).style.display = '';
      }
    </script>
    <title>Partychapp</title>
  </head>
  <body>
  <h2>Welcome to Partychat</h2>
  
  <p>Partychapp lets you easily create chat rooms using XMPP/Google Talk accounts</p>
  
  <h3>Create a room!</h3>

<%
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();
    if (user != null) {
%>
<div id="actionOptions">
<input type="button" value="Create a new room" onclick="show('create')"/>
</div>
<div id="create" style="display: none; border: 1px solid #ccc">
<table cellpadding=10><tr><td>
<form action="/room" method="post" target="createResults">
Pick a room name<br>
<input type="text" name="name">
<br><br>
Do you only want people who are invited to be able to join?<br>
<input type="radio" name="inviteonly" value="true" checked="yes"> yes
<input type="radio" name="inviteonly" value="false"> no
<br><br>
Email addresses you would like to invite? (separated by commas)<br>
<textarea name="invitees"></textarea>
<br><br>
<input type="submit" value="Create!">
</form>
</td><td>
<iframe frameborder=0 name="createResults">
</iframe>
</td></tr></table>
</div>

<%
    } else {
%>
The easiest way to create or join a room is to <a href="<%= userService.createLoginURL(request.getRequestURI()) %>">sign in</a>.
<br/><br/>
Or you can add [roomname]@partychapp.appspotchat.com to your buddy list and send it a message to join the room. If a room of that name doesn't exist, a new one will be created.
<%
    }
%>





  
  
  <h3>Commands:</h3>
  <ul><li>Send "/list" to see a list of all the users in the room</li>
  <li>Send "/leave" to remove yourself from the room.</li>
  <li>...more to come!</li></ul>
  
  <h3>Is this thing on?</h3>
  <p>Partychapp is alpha software, and under active development.  Sometimes things will break.
  If you suspect that Partychapp may be messing up, you can send a message to <b>echo</b>@partychapp.appspotchat.com.
  If Partychapp is working, it will send back an echo.  If not, <a href="mailto:imjasonh@gmail.com">e-mail or IM me</a> and I'll try desperately to fix it.</p>
  </body>
</html>