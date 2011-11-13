#!/usr/bin/env python

import BaseHTTPServer, cgi
from SimpleComponent import StateManager

class HTTPFrontend :
	class RequestHandler (BaseHTTPServer.BaseHTTPRequestHandler) :
		def do_GET(self) :
			sm = StateManager.instance()
			self.wfile.write("channels %d\n" % sm.num_channels())
			g,b,t = sm.num_ok_bad_total_users()
			
			self.wfile.write("good_users %d\n" % g)
			self.wfile.write("bad_users %d\n" % b)
			self.wfile.write("total_users %d\n" % t)
			
			for t,k,v in sm.counters_as_tuples():
				self.wfile.write('%s_%s_count %d\n' % (t,k,v))


	def __init__(self, port, backend) :
		self.server = BaseHTTPServer.HTTPServer(('',port), self.RequestHandler)
		print "Web interface listening on http://localhost:" + str(port)
	
	def start(self) :
		self.server.serve_forever()
	
	def stop(self) :
		self.server.socket.close()
