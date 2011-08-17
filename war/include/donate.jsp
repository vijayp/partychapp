<!-- Common markup, meant to be included at the start of the <body> section -->
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="com.imjasonh.partychapp.Configuration" %>
<%@ page import="com.imjasonh.partychapp.Datastore"%>
<%@ page import="com.imjasonh.partychapp.server.HttpUtil"%>
<p>
Partychat doesn't cost you anything. However, as usage has grown substantially over 
the past year, App Engine usage costs are getting pretty high. 

<p><a href="http://partychapp.appspot.com/#about_us">We</a> 
have so far been paying these charges ourselves.  If you use and enjoy Partychat, please consider donating 
some money to help us defray our costs. (Payments are processed via Amazon Simple Pay.)</p>

<p> $15 lets us keep Partychat running for one week;  $60 keeps us going for a month!</p> 

<div style="width:20em;padding-left:10px;padding-top:10px;padding-right:10px;padding-bottom:10px;">
<form action="https://authorize.payments.amazon.com/pba/paypipeline" method="post">
  <input type="hidden" name="immediateReturn" value="1" >
  <input type="hidden" name="collectShippingAddress" value="0" >
  <input type="hidden" name="accessKey" value="11SEM03K88SD016FS1G2" >
  Donate 
  <input type=number name="amount" value="10" required min="1.0" max="1000" step="1" size="10px"> 
  Dollars
  <input type="hidden" name="isDonationWidget" value="0" >
  <input type="hidden" name="description" value="Donation to PartyChat" >
  <input type="hidden" name="amazonPaymentsAccountId" value="HQZPNBWETEVX66TDFIF4SBRUBQIPVJFGB3UXHS" >
  <input type="hidden" name="returnUrl" value="http://partychapp.appspot.com/donate_done.jsp" >
  <input type="hidden" name="processImmediate" value="1" >
  <input type="hidden" name="cobrandingStyle" value="logo" >
<%
// This is not used by amazon in our configuration
//<input type="hidden" name="abandonUrl" value="http://partychapp.appspot.com/donate_abandoned.jsp" >
%>
  <input type="image" src="http://g-ecx.images-amazon.com/images/G/01/asp/beige_small_paynow_withmsg_whitebg.gif" border="0">
</form>
</div>	
