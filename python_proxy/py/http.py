import tornado.httpserver
import tornado.httpclient
import tornado.ioloop
import tornado.web
import simplejson as json

TOKEN = 'tokendata'
class MainHandler(tornado.web.RequestHandler):
  def get(self):
    self.redirect('http://partych.at')

class InboundControlHandler(tornado.web.RequestHandler):
  def initialize(self, component):
    self._component = component

  def post(self):
    token = self.get_argument('token')
    if token != TOKEN:
      raise tornado.web.HTTPError(403)
    body = json.loads(self.get_argument('body'))
    self._component._handle_control_message(body)
    self.write('"OK"')

  def get(self):
    self.post()


class OutboundHandler:
  def __init__(self):
    tornado.httpclient.AsyncHTTPClient.configure("tornado.curl_httpclient.CurlAsyncHTTPClient")
    self._http_client = tornado.httpclient.AsyncHTTPClient()
    self.outstanding_requests = 0

  def handle_resp(self, r):
    logging.info('POST         RESPONSE: %s', str(r))
    self.outstanding_requests -= 1

  def post(url, params):
    body = urllib.urlencode(params)

    logging.info('POST          Sending data to %s', url)
    req_obj     = tornado.httpclient.HTTPRequest(url,
                                               method='POST',
                                               body=body)
    self._http_client(req_obj, self.handle_resp)
    self.outstanding_requests += 1

