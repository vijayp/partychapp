<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="com.imjasonh.partychapp.Configuration" %>
<%@ page import="com.imjasonh.partychapp.Datastore"%>
<%@ page import="com.imjasonh.partychapp.server.HttpUtil"%>
<%@ page import="java.util.Date" %>
<%@ page import="com.google.appengine.api.datastore.DatastoreService"%>
<%@ page import="com.google.appengine.api.datastore.DatastoreServiceFactory"%>
<%@ page import="com.google.appengine.api.datastore.Entity"%>

<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
<!DOCTYPE html>
<html>
<head>
  <jsp:include page="include/head.jsp"/>
</head>
<body>
<jsp:include page="include/header.jsp"/>

Thank you for your donation to PartyChat. 

<a href="http://partychapp.appspot.com"> Return to the home page</a>


<code>
<%
DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

String[] params = {"transactionId", "referenceId", "operation", "paymentReason", "transactionAmount", "transactionDate", "paymentMethod", "recipientEmail", "buyerEmail"};
Entity donation = new Entity("Donation");
for (String p : params) {
  donation.setProperty(p, request.getParameter(p));	
}
donation.setProperty("now",  new Date());
//out.println(org.apache.commons.lang.StringEscapeUtils.escapeHtml(donation.toString()));
datastore.put(donation);

%>
</code>
<jsp:include page="include/footer.jsp"/>
</body>
</html>