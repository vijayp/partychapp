<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="com.imjasonh.partychapp.Configuration" %>
<!DOCTYPE html>
<html>
<head>
  <jsp:include page="include/head.jsp"/>
</head>
<body>
<jsp:include page="include/header.jsp"/>

<p>Create chat rooms with your friends or coworkers using Google
Talk or XMPP.</p>

<h3>Why use Partychat?</h3>
<ul>
	<li>Use whatever you use to chat already: GMail, Adium,...</li>
	<li>Catch up on messages you miss while offline.</li>
	<li>Don't have to re-join rooms when you log-out.</li>
	<li>Built on reliable Google App Engine.</li>
	<li>Easy to use, lots of <a href="#nowwhat">silly features</a>.</li>
</ul>
<%
	if (Configuration.isConfidential) {
%>
<h3>Are messages confidential?</h3>
Yup! We're running on an internal instance of AppEngine, so everything
stays safe. <%
  	}
  %>
<h3>How do I create a room?</h3>

<%
	UserService userService = UserServiceFactory.getUserService();
	User user = userService.getCurrentUser();

	if (user != null) {
%>
<div id="create-button-container">
  <input type="button" value="Create a new room" onclick="showCreateForm()" />
</div>
<form onsubmit="return submitCreateRoom()">
<table id="create-table" class="hidden">
  <tr>
    <td class="label">Room name:</td>
    <td><input type="text" size="40" id="room-name"></td>
  </tr>
  <tr>
    <td class="label">Room type:</td>
    <td>
      <table class="inviteonly-table">
        <tr>
          <td><input type="radio" name="inviteonly" value="true" checked="yes" id="inviteonly-true"></td>
          <td><label for="inviteonly-true">Invite-only</label></td>
        </tr>
        <tr class="description">
          <td></td>
          <td>Only invited people may join</td>
        </tr>
      </table>

      <table class="inviteonly-table">
        <tr>
          <td><input type="radio" name="inviteonly" value="false" id="inviteonly-false"></td>
          <td><label for="inviteonly-false">Open</label></td>
        </tr>
        <tr class="description">
          <td></td>
          <td>Anyone may join</td>
        </tr>
      </table>
    </td>
  </tr>
  <tr>
    <td class="label">
      Others to invite:
      <div class="description">Email addresses,<br>separated by commas</div>
    </td>
    <td>
      <textarea id="invitees" rows="4"></textarea>
    </td>
  </tr>
  <tr>
    <td class="buttons" colspan="2">
      <input type="submit" value="Create!">
    </td>
  </tr>
</table>

<div id="create-result" class="hidden"></div>
</form>

<jsp:include page="include/userinfo.jsp"/>

<%
	} else {
%> The easiest way to create a room is to <a style="font-weight: bold"
	href="<%=userService.createLoginURL(request.getRequestURI())%>">sign
in</a> and do it right here. <br />
<br />
Or you can add <tt>[roomname]@<%=Configuration.chatDomain%></tt> to your
buddy list and send it a message to join the room. If a room of that
name doesn't exist, a new one will be created. <%
	}
%>

<h3>How do I join a room?</h3>
The easiest way to join a room is to be invited. If the room has already
been created, have someone in the room type <tt>/invite
youremailaddress@anydomain.com</tt>. <br>
<br>
You should see an invitation from <tt>[roomname]@<%=Configuration.chatDomain%></tt>
in your chat window. Accept the invitation, and then <b>send a
message to your new buddy</b>, such as "hi." This will finish adding you to
the room. <br>
<br>
Alternatively, if a room is not invite-only, you can just add <tt>[roomname]@<%=Configuration.chatDomain%></tt>
to your buddy list and send it a message. <a name="nowwhat">
<h3>Okay, I'm in a room, now what?</h3>
Besides just sending messages and having everyone see them, most of the
things you can do take the form of commands you type as special chat
messages starting with a /.<br>
<br>
<img
	src="http://1.bp.blogspot.com/_qxrodbRnu8Q/SyL57yANfsI/AAAAAAAAD4w/pRdYP3wI_a4/s400/pchapp-shot.png">
<br>
<br>
You can get a full list of commands by sending the chat message <tt>/help</tt>
to the room. Some key ones:
<ul>
	<li><tt>/leave</tt> Leave this chat room. You can rejoin by
	sending another message to the room. If the room is invite-only, you
	may need to be re-invited.</li>
	<li><tt>/list</tt> See who is in the chat room.</li>
	<li><tt>/alias <i>newalias</i></tt> Change what name you show up
	as in the room.</li>
	<li><tt>/inviteonly</tt> Toggle whether this room is invite only.</li>
	<li><tt>/invite <i>someemail</i></tt> Invite someone to the room.</li>
	<li><tt>/me <i>someaction</i></tt> Tell the room what you're up
	to. If you type <tt>/me is rolling his eyes</tt>, everyone sees <tt>[youralias]
	is rolling his eyes</tt>.</li>
	<li><tt>/score <i>something</i></tt> This one's a bit complicated.
	You can give points to things you like by typing ++ at the end of them
	in your message. For example, you might say <tt>partychat++ for
	being so handy</tt>. This adds one to the score for partychat, which you can
	see by typing <tt>/score partychat</tt>. Or you can take points away
	from things you dislike, such as <tt>kushal-- for another bad pun</tt>.

</ul>

<h3>Tell me more about this "partychat"</h3>
Partychat was started by <a href=http://www.q00p.net>Akshay</a> and is
maintained by a motley, ragtag group of current and former Googlers with
names like Neil, Jason, Kushal, Vijay, and Mihai, although <i>this is not in
any way associated with Google</i>. You can find the source code on <a
	href="http://code.google.com/p/partychapp/">Google Code</a>. <br>
<br>
For updates, please subscribe to our <a
	href="http://techwalla.blogspot.com/">blog</a> or <a
	href="http://twitter.com/partychat">follow us on Twitter</a>.

<jsp:include page="include/footer.jsp"/>
</body>
</html>