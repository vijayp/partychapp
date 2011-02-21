<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ page import="com.imjasonh.partychapp.Channel"%>
<%@ page import="com.imjasonh.partychapp.server.web.ChannelShareServlet.ShareData"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<!DOCTYPE html>
<html>
<head>
<%
  Channel channel = (Channel) request.getAttribute("channel");
  ShareData shareData = (ShareData) request.getAttribute("shareData");
%>
<jsp:include page="include/head.jsp">
  <jsp:param name="subtitle" value="<%=&quot;Share with room &quot; + channel.getName()%>"/>
</jsp:include>
</head>
<body class="channel">
  <jsp:include page="include/header.jsp">
    <jsp:param name="subtitle" value="<%=&quot;Share with room &quot; + channel.getName()%>"/>
  </jsp:include>

<form action="/channel/share" method="POST">
<input type="hidden" name="name" value="<%=channel.getName()%>"/>
<table id="channel-share-table" class="channel-form-table">
  <tr>
    <td class="label">URL:</td>
    <td>
      <input type="text" name="url" value="${fn:escapeXml(shareData.url)}" id="url">
      <% if (!shareData.getTitle().isEmpty()) { %>
        <div class="url-info"><b>Title:</b> ${fn:escapeXml(shareData.title)}</div>
      <% } %>
      <% if (!shareData.getDescription().isEmpty()) { %>
        <div class="url-info"><b>Description:</b> <%=shareData.getDescription()%></div>
      <% } %>
    </td>
  </tr>

  <tr>
    <td class="label">Annotation:</td>
    <td>
      <input type="text" name="annotation" id="annotation">
    </td>
  </tr>

  <tr>
    <td class="buttons" colspan="2">
      <input type="submit" value="Share">
    </td>
  </tr>
</table>
</form>

<jsp:include page="include/footer.jsp"/>
</body>
</html>
