#!/usr/bin/env python

import BaseHTTPServer, cgi


class HTTPFrontend :
	class RequestHandler (BaseHTTPServer.BaseHTTPRequestHandler) :
		def do_GET(self) :
			self.wfile.write("TODO: make this do something")

	def __init__(self, port, backend) :
		self.server = BaseHTTPServer.HTTPServer(('',port), self.RequestHandler)
		print "Web interface listening on http://localhost:" + str(port)
	
	def start(self) :
		self.server.serve_forever()
	
	def stop(self) :
		self.server.socket.close()
