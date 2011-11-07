#!/usr/bin/env python

import logging
#from SimpleBackend import SimpleBackend
from HTTPFrontend import HTTPFrontend
from SimpleComponent import SimpleComponent

# Uncomment the following line to turn on debugging
logging.basicConfig(level=logging.INFO, format='%(levelname)-8s %(message)s')

def main() :
	component = SimpleComponent(
		jid = "im.partych.at", password = "secret",
		server = "127.0.0.1", port = 5275, backend = None)
	component.start()
	httpFrontend = HTTPFrontend(8080, None)
	httpFrontend.start()

if __name__ == '__main__' :
	main()
