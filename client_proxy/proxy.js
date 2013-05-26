fs = require("fs");
https = require('https');


var querystring = require('querystring');

var privateKey = fs.readFileSync('/etc/certs/privatekey.pem').toString();
var certificate = fs.readFileSync('/etc/certs/certificate.pem').toString();
var token = fs.readFileSync('/etc/certs/token').toString();
var xmppClients = {};
  // An object of options to indicate where to post to

var getClient = function(username, password) {
    var KEY_SEPARATOR = '\u009E'; // some random UTF-8 control character
    var cl = xmppClients[username+KEY_SEPARATOR+password];
    if (!cl) {
	cl = require('simple-xmpp')
	cl.connect({jid:username, password:password});
	cl._myJid = username;
	cl.on('online',
	      function() {
		  console.log('GOT ONLINE');
	      });
	cl.on('chat', function(from, message) {

	    console.log('inbound message ', from, message);
/*      root["message_str"] = *body;
      root["to_str"] = to->bare();
      root["from_str"] = from->bare();
      root["state"] = "old";
*/
	    out = {'message_str' : message, 'to_client_proxy' : cl._myJid, from_str:from, state:'old'};
	    outStr = JSON.stringify(out);
	    var post_data = querystring.stringify({
		'body': outStr,
		'token':token});

	    var post_options = {
		host: 'partychapp.appspot.com',
		port: '443',
		path: '/___control___',
		method: 'POST',
		headers: {
		    'Content-Type': 'application/x-www-form-urlencoded',
		    'Content-Length': post_data.length
		}
	    };
            console.log('posting', post_data);
	    var post_req = https.request(post_options, function(res) {
		res.setEncoding('utf8');
		res.on('data', function (chunk) {
		    console.log(chunk);
		    body = JSON.parse(chunk);
		    cl.sendProxied(body);
		});
	    });

  // post the data
	    post_req.write(post_data);
	    post_req.end();
 
	});
	cl.on('subscribe', function(from) {
	    cl.acceptSubscription(from);
	});
	cl._myFriends = {};
	cl.sendProxied = function(body) {
	    cl.getRoster();
	    var _sendProxied = function() {
		to = body.recipients.pop();
		if (to === undefined) {
		    return;
		}
		console.log('sending to: ', to, ' message: ', body.outmsg, ' via:', body.gmail_username);
		if (!cl._myFriends[to]) {
		    cl.subscribe(to);
		    cl._myFriends[to] = true;
		};
		
		cl.send(to, body.outmsg);
		setTimeout(_sendProxied, 150);
	    };
	    _sendProxied();
	};
	xmppClients[username + KEY_SEPARATOR+password] = cl;
    };
    return cl;
};


var options = {
    key: privateKey,
    cert: certificate
};
function postRequest(request, response, callback) {
    var queryData = "";
    if(typeof callback !== 'function') return null;

    if(request.method == 'POST') {
        request.on('data', function(data) {
            queryData += data;
            if(queryData.length > 1e6) {
                queryData = "";
                response.writeHead(413, {'Content-Type': 'text/plain'}).end();
                request.connection.destroy();
            }
        });

        request.on('end', function() {
            response.post = querystring.parse(queryData);
            callback();
        });

    } else {
        response.writeHead(405, {'Content-Type': 'text/plain'});
        response.end();
    }
}

var server = https.createServer(options, function (req, res) {
    postRequest(req,res, function() {
	console.log(res.post);
	var body  = JSON.parse(res.post.body);
	var client = getClient(body.gmail_username, body.gmail_password);
	client.sendProxied(body);
	res.writeHead(200);
	res.end("OK\n\n");
    });
});

server.listen(4443);
