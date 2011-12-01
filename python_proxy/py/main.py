#!/usr/bin/env python

import logging
#from SimpleBackend import SimpleBackend
from SimpleComponent import SimpleComponent, SUBDOMAIN, do_exit, SAVE, StateManager
import signal

import time
import cProfile
from pwd import getpwnam
import os

import tornado.httpserver
import tornado.httpclient
import tornado.ioloop
import tornado.web
import socket

from http import *

ch = logging.StreamHandler()
ch.setLevel(logging.INFO)
formatter = logging.Formatter(
    "%(levelname).1s%(asctime)s %(filename)s:%(lineno)d] %(message)s",
    datefmt='%Y%m%d:%H%M%S')
ch.setFormatter(formatter)
for L in [logging.getLogger(''), logging.getLogger()]:
  L.setLevel(logging.INFO)
  L.addHandler(ch)



import urllib

import argparse
parser = argparse.ArgumentParser()
parser.add_argument('--db_host', default='10.220.227.98')
parser.add_argument('--db_port', type=int, default=27017)
parser.add_argument('--port', type=int, default=80)
parser.add_argument('--ssl_port', type=int, default=443)
parser.add_argument('--db_collection', default='channel_state')
parser.add_argument('--jabber_host', default='10.220.227.98')
parser.add_argument('--jabber_port', type=int, default=5275)
parser.add_argument('--subdomain', default=SUBDOMAIN)

FLAGS = parser.parse_args()

from sleekxmpp.xmlstream import PROFILERS

def main() :
        
        oh = OutboundHandler()
        StateManager.Init(FLAGS.db_host, FLAGS.db_port, FLAGS.db_collection)
        
	component = SimpleComponent(
		jid = FLAGS.subdomain + ".partych.at", 
                password = "secret",
		server = FLAGS.jabber_host, port = FLAGS.jabber_port, oh = oh)
#		server = "127.0.0.1", port = 5275, oh = oh)
        oh.initialize(component)
	component.start()



        application = tornado.web.Application([
            (r"/", MainHandler),
            (r"/varz", VarzHandler),
            (r'/___control___', InboundControlHandler, 
             dict(component=component)),
            ])
        data_dir='/etc/certs'
        server = tornado.httpserver.HTTPServer(application,
           ssl_options={"certfile": os.path.join(data_dir, "server.crt"),
           "keyfile": os.path.join(data_dir, "server.key"),
       })

        ports = range(FLAGS.port+10, FLAGS.port-1, -1)
        ssl_ports = range(FLAGS.ssl_port+10, FLAGS.ssl_port-1, -1)
        while ports and ssl_ports:
          try:
            p = ports.pop()
            sp = ssl_ports.pop()
            logging.info('trying port %s,%s', p,sp)
            application.listen(p)
            server.listen(sp)
            break
          except socket.error:
            logging.info('port failed')
            
        else:
          logging.error('cannot bind to http ports, quitting')
          sys.exit(-1)

        logging.info('dropping permissions')

        os.setuid(getpwnam('nobody').pw_uid)
        def print_profiles():
          for k,(p,name) in enumerate(PROFILERS):
            logging.info('THREAD STATS dumping stats for thread %s', name)
            p.dump_stats('%s.profile'% name)

        tornado.ioloop.PeriodicCallback(print_profiles, 60000).start()
        def runner(name, target):
          import cProfile
          profiler = cProfile.Profile()
          PROFILERS.append((profiler,name))
          logging.info('THREAD starting  %s',
                       name)
          profiler.runcall(target)
            
        runner('ioloop', tornado.ioloop.IOLoop.instance().start)
        

if __name__ == '__main__' :
  main()

