<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ page import="java.util.Collections"%>
<%@ page import="java.util.List"%>
<%@ page import="com.google.common.collect.Lists"%>
<%@ page import="com.imjasonh.partychapp.Channel"%>
<%@ page import="com.imjasonh.partychapp.Datastore"%>
<%@ page import="com.imjasonh.partychapp.Member"%>
<%@ page import="com.imjasonh.partychapp.ppb.Reason"%>
<%@ page import="com.imjasonh.partychapp.ppb.Target"%>

<!DOCTYPE html>
<html>
<head>
<%
  Channel channel = (Channel) request.getAttribute("channel");
  Datastore datastore = Datastore.instance();
%>
<jsp:include page="include/head.jsp">
  <jsp:param name="subtitle" value="<%="Channel Stats: " + channel.getName()%>"/>
</jsp:include>
</head>
<body>
  <jsp:include page="include/header.jsp"/>

<div class="channelName">Channel stats for:
<span style="font-weight:bold"><%=channel.getName()%></span></div>

<h3>PlusPlusBot</h3>

<table class="channel-table">
  <tr>
    <th class="target-cell">Target</th>
    <th class="score-cell">Score</th>
  </tr>
  <%
    List<Target> targets = datastore.getTargetsByChannel(channel);
    for (Target t : targets) {
  %>

  <tr>
    <td class="target-cell">
      <div class="target-name" onclick="toggleTargetDetails(this, '<%=channel.getName()%>', '<%=t.name()%>')">
        <%=t.name()%>
      </div>
    </td>
    <td class="score-cell"><%=t.score()%></td>
  </tr>
<% } %>
</table>

<h3>Members</h3>
<table class="channel-table">
  <tr>
    <th>Alias</th>
    <th>Email address</th>
  </tr>
  <%
    List<Member> members = Lists.newArrayList(channel.getMembers());
    Collections.sort(members, new Member.SortMembersForListComparator());
    for (Member m : members) {
  %>
  <tr>
    <td><%=m.getAlias()%></td>
    <td><%=m.getJID()%></td>
  </tr>
  <% } %>
</table>

<% if (!channel.getInvitees().isEmpty()) {%>
  <h3>Invited</h3>
  <table class="channel-table">
    <tr>
      <th>Email address</th>
    </tr>
    <%
      List<String> invitedMembers = Lists.newArrayList(channel.getInvitees());
      Collections.sort(invitedMembers);
      for (String invitedMember : invitedMembers) {
    %>
      <tr>
        <td><%=invitedMember%></td>
      </tr>
    <% } %>
  </table>
<% } %>

<h3>Invite People!</h3>

<table>
  <tr>
    <td>
    <form action="/invite" method="post" target="inviteResults">
    <input type="hidden" name="name" value="<%=channel.getName()%>"/>
    Email addresses you would like to invite? (separated by commas)<br>
    <textarea name="invitees" rows="4" cols="40"></textarea> <br>
    <br>
    <input type="submit" value="Invite!"></form>
    </td>
    <td><iframe frameborder=0 name="inviteResults"> </iframe></td>
  </tr>
</table>

<jsp:include page="include/footer.jsp"/>
</body>
</html>
