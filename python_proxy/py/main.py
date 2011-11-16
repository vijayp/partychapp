#!/usr/bin/env python

import logging
#from SimpleBackend import SimpleBackend
from HTTPFrontend import HTTPFrontend
from SimpleComponent import SimpleComponent, SUBDOMAIN, do_exit, SAVE
import signal

import time
import cProfile
from pwd import getpwnam
import os

import tornado.httpserver
import tornado.httpclient
import tornado.ioloop
import tornado.web


ch = logging.StreamHandler()
ch.setLevel(logging.INFO)
formatter = logging.Formatter(
    "%(levelname).1s%(asctime)s %(filename)s:%(lineno)d] %(message)s",
    datefmt='%Y%m%d:%H%M%S')
ch.setFormatter(formatter)
for L in [logging.getLogger(''), logging.getLogger()]:
  L.setLevel(logging.INFO)
  L.addHandler(ch)

class MainHandler(tornado.web.RequestHandler):
    def get(self):
        self.write("Hello, world")

application = tornado.web.Application([
    (r"/", MainHandler),
])
import urllib

def handle_resp(r):
  logging.info('POST         RESPONSE: %s', str(r))

def post(url, params):
  body = urllib.urlencode(params)
  http_client = tornado.httpclient.AsyncHTTPClient()
  logging.info('POST          Sending data to %s', url)
  req_obj     = tornado.httpclient.HTTPRequest(url,
                                               method='POST',
                                               body=body)
  http_client.fetch(req_obj, handle_resp)
  

def main() :
        logging.info('dropping permissions')
        os.setuid(getpwnam('nobody').pw_uid)
        
	signal.signal(signal.SIGTERM, do_exit)
	signal.signal(signal.SIGHUP, SAVE)

	component = SimpleComponent(
		jid = SUBDOMAIN + ".partych.at", password = "secret",
		server = "10.220.227.98", port = 5275, post = post)
#		server = "127.0.0.1", port = 5275, backend = None)
	component.start()

        application.listen(8889)
        tornado.ioloop.IOLoop.instance().start()
        

if __name__ == '__main__' :
  cProfile.run('main()', 'profile_data.' + str(time.time()))

