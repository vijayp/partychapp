<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ page import="com.imjasonh.partychapp.Channel"%>

<!DOCTYPE html>
<html>
<head>
<%
  Channel channel = (Channel) request.getAttribute("channel");
  String token = (String) request.getAttribute("token");
%>
<jsp:include page="include/head.jsp">
  <jsp:param name="subtitle" value="<%=&quot;Room &quot; + channel.getName()%>"/>
</jsp:include>
  <script type="text/javascript" src="/_ah/channel/jsapi"></script>
  <style>
    .message {
      white-space: pre;
    }
  </style>
</head>
<body class="channel">
  <jsp:include page="include/header.jsp">
    <jsp:param name="subtitle" value="<%=channel.getName() + &quot; - Live&quot;%>"/>
  </jsp:include>
  
  <div id="messages"></div>

  <script>
    function sendCommand(command) {
      var url = '/channel/live/' + command + '?name=' + encodeURIComponent('<%=channel.getName()%>');
      new Image().src = url;    
    }
    
    function ping() {
      sendCommand('ping');
    }

    function leave() {
      sendCommand('leave');
    }
    
    onunload = leave;

    var messagesNode = document.getElementById('messages');
    var channel = new goog.appengine.Channel('<%=token%>');
    var socket = channel.open({
      onopen: function() {
        setInterval(ping, 5 * 60 * 1000);
      },
      onmessage: function(message) {
        var data = eval('(' + message.data + ')');
        var messageNode = document.createElement('div');
        messageNode.className = 'message';
        messageNode.appendChild(document.createTextNode(data.message));
        messagesNode.insertBefore(messageNode, messagesNode.firstChild);
      },
      onerror: function(error) {
        console.log('channel error');
        console.dir(error);
      },
      onclose: function() {
        leave();
      }
    });
  </script>

<jsp:include page="include/footer.jsp"/>
</body>
</html>
