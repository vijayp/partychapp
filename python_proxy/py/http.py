import tornado.httpserver
import tornado.httpclient
import tornado.ioloop
import tornado.web
import simplejson as json
import urllib
import logging
import time
from collections import deque, Counter, defaultdict

class VarzDatum:
  def __init__(self, name='', oldest=60*60*24):
    self._name = name
    self._history = deque()
    self._older = 0
    self._oldest = oldest

  def add(self, count=1):
    self._history.append((time.time(), count))
    self._prune_front()

  def _prune_front(self):
    cutoff = time.time() - self._oldest
    while self._history and self._history[0][0] < cutoff:
      # todo make this o(lgn) amortized
      popped = self._history.popleft()
      self._older += popped[1]

  def strs(self,
          quanta=[10,60,60*10, 60*60, 60*60*10],
          name=''):
    for suffix, count in self.get_for_quanta(quanta):
      yield '%s_%s %s' % (name if name else self._name, suffix, count)

  def get_for_quanta(self, quanta):
    self._prune_front()
    quanta = deque(sorted(quanta))
    c = Counter()
    c[-1] = self._older
    now = time.time()
    i = len(self._history) -1 
    while i>=0 and quanta:
      if self._history[i][0] >= now - quanta[0]:
        for q in quanta:
          c[q] += self._history[i][1]

        c[-1] += self._history[i][1]
        i-=1
      else:
        quanta.popleft()
    return c.iteritems()


class VZContainerType(defaultdict):
  def str_items(self):
    for k,v in self.iteritems():
      for x in v.strs(name=k):
        yield x

Stats = VZContainerType(VarzDatum)      
  

TOKEN = 'tokendata'
class MainHandler(tornado.web.RequestHandler):
  def get(self):
    self.redirect('http://partych.at')


class VarzHandler(tornado.web.RequestHandler):
  def get(self):
    self.set_header("Content-Type", "text/plain") 
    self.write('loadavg %s' % open('/proc/loadavg').read())
    self.write('\n'.join(sorted(Stats.str_items())))
    
    
class InboundControlHandler(tornado.web.RequestHandler):
  def initialize(self, component):
    self._component = component

  def post(self):
    Stats['inbound_post_requests_received'].add()
    token = self.get_argument('token')
    if token != TOKEN:
      raise tornado.web.HTTPError(403)
    body = json.loads(self.get_argument('body'))
    self._component.xmpp.event('CONTROL', body)
    logging.info('INPOST                     message from control')
    Stats['inbound_post_requests_ok'].add()
    self.write('"OK"')

  def get(self):
    self.post()


class OutboundHandler:
  def __init__(self):
#    tornado.httpclient.AsyncHTTPClient.configure("tornado.curl_httpclient.CurlAsyncHTTPClient")
    self.outstanding_requests = 0

  def handle_resp(self, r):
#    logging.info('POST         RESPONSE: %s', str(r))
    Stats['outbound_post_requests_received'].add()
    self.outstanding_requests -= 1

  def post(self, url, params):
    Stats['outbound_post_requests_sent'].add()
    body = urllib.urlencode(params)
    logging.info('POST          Sending data to %s', url)
    req_obj     = tornado.httpclient.HTTPRequest(url,
                                               method='POST',
                                               body=body)
    http_client = tornado.httpclient.AsyncHTTPClient()
    http_client.fetch(req_obj, self.handle_resp)
    self.outstanding_requests += 1

  
if __name__ == '__main__':
  
  time.time = lambda:0
  Stats['hi'].add(1)
  Stats['there'].add(33)
  time.time = lambda:122222223
  Stats['hi'].add(33)
  Stats['hi']
  print '\n'.join(Stats.str_items())
