#!/usr/bin/env python

import logging
#from SimpleBackend import SimpleBackend
from HTTPFrontend import HTTPFrontend
from SimpleComponent import SimpleComponent, SUBDOMAIN, do_exit, SAVE
import signal

import time
import cProfile


# Uncomment the following line to turn on debugging
logging.basicConfig(level=logging.INFO, format='%(levelname)-8s %(message)s')

def main() :
	signal.signal(signal.SIGTERM, do_exit)
	signal.signal(signal.SIGHUP, SAVE)

	component = SimpleComponent(
		jid = SUBDOMAIN + ".partych.at", password = "secret",
		server = "127.0.0.1", port = 5275, backend = None)
	component.start()
	httpFrontend = HTTPFrontend(8081, None)
	httpFrontend.start()

if __name__ == '__main__' :
  cProfile.run('main()', 'profile_data.' + str(time.time()))

