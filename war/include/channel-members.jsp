<!-- Table of members of a channel -->

<%@ page import="java.util.Collections"%>
<%@ page import="java.util.List"%>
<%@ page import="com.google.common.collect.Lists"%>
<%@ page import="com.imjasonh.partychapp.Channel"%>
<%@ page import="com.imjasonh.partychapp.Member"%>

<%
  Channel channel = (Channel) request.getAttribute("channel");
%>

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