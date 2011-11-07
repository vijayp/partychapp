import zlib
import sys
import sleekxmpp.componentxmpp
import logging
import simplejson as json
import zlib
from collections import defaultdict

MYDOMAIN = 'im.partych.at'

PARTYCHAPP_CONTROL = '__control@partychapp.appspotchat.com'
#PARTYCHAPP_CONTROL = 'newpc_test@partychapp.appspotchat.com'
MY_CONTROL = '_control@im.partych.at'
STATUS = 'replacing app engine since 2011'
import time

class State:
  UNKNOWN = 0
  PENDING = 1
  REJECTED = 2
  OK = 3
  def __init__(self):
    self._state = State.UNKNOWN
    self._timestamp = time.time()
  def should_request(self):
    # TODO: remember to add in time data into the mix.
    return not self._state in [State.PENDING, State.REJECTED, State.OK]

  def request_pending(self):
    # TODO: remember to add in time data into the mix.
    self._state = State.PENDING
    # TODO: we never actually transition out of pending for now ...
    
    
  def save(self):
    # unimplemented
    pass
  def load(self):
    # unimplemented
    pass

class SimpleComponent:
  @staticmethod
  def GetControlMessage(event):
    
    if str(event['to']) != MY_CONTROL:
#      logging.info('to is <%s> but not <_control@im.partych.at>', str(event['to']))
      return None
    else:
      assert str(event['from']).startswith(PARTYCHAPP_CONTROL)
      # TODO: check from, and check signature of message
      msg_str = str(event['body'])
      if msg_str.startswith('gzip:'):
	try:
          msg_str = zlib.decompress(msg_str[len('gzip:'):])
        except:
          open('/tmp/broken', 'wb').write(msg_str)
#      logging.info('decoding <%s>', msg_str)
      return json.loads(msg_str)

    
  def __init__(self, jid, password, server, port, backend) :
    
    self.xmpp = sleekxmpp.componentxmpp.ComponentXMPP(jid, password, server, port)
    self._state = defaultdict(State)

    self.xmpp.auto_authorize = None
    self.xmpp.auto_subscribe = None
    self.xmpp.add_event_handler('session_start', self.start_session)
    self.xmpp.add_event_handler('message', self.message)

    self.xmpp.del_event_handler('presence_probe', 
                                self.xmpp._handle_probe)
    for s in ['presence_subscribe', 
              'presence_subscribed', 
              'presence_unsubscribe', 
              'presence_unsubscribed',
              'presence_probe',
              'presence_available',
              'presence_error',
              'got_online',
              'got_offline',
              'changed_status',
              'changed_subscription',
              'message',
              'message_form',
              'message_xform',
              'presence_form',
              'roster_update',
              'sent_presence',
              ]:
      self.xmpp.add_event_handler(s, lambda event: self.generic_handler(s, event))
    self._jid_resource_map = {}
    
    

  def _inbound_message(self, message):
    # echo
    self.xmpp.sendMessage(str(message['from']).split('/')[0], message['body'], 
                          mfrom=message['to'], 
                          mtype='chat')

  def message(self, message):
    if not str(message['from']).startswith('vijayp'):
      return

    logging.info(' MESSAGE: %s' , message)
    if message['type'] == 'error':
      logging.error('ERROR: %s', message)
      logging.info('requesting subscription')
      from_person = message['from']
      if self._state[from_person].should_request():
        self._state[from_person].request_pending()
        self.xmpp.sendPresence(pto=message['from'], pfrom=message['to'],
                               ptype='subscribe')

    else:
      self._inbound_message(message)

  def generic_handler(self, s, event):
    try:
      if not str(event['from']).startswith('vijayp'):
        return
    except:
      return
    logging.info('generic handler for event %s (%s)', s, event)
    if not event:
      return
    if s == 'roster_update':
      pass
      self.xmpp.sendPresence(pto=event['from'], pfrom=event['to'], pstatus="test")

  def send_unsubscribe(self, u, f):
    self.xmpp.sendPresence(pto=u, pfrom=f, 
                           ptype="presence_unsubscribed")
    

  def start_session(self, *args, **kwargs):
    logging.info('started session')
    logging.info('trying to probe vijayp@gmail.com for dogfood')
#    u = 'test@partych.at'
    u = 'vijayp@gmail.com'
#    self.send_unsubscribe(u, 'dogfood@im.partych.at')
    self.xmpp.sendPresence(pto=u, pfrom="dogfood@im.partych.at",
                           pstatus='test')

#    self.xmpp.sendPresence(pto=u, pfrom="dogfood@im.partych.at", pstatus="test",
#                           ptype="subscribe")
#    self.xmpp.sendPresence(pto=u, pfrom="dogfood@im.partych.at", pstatus="test")

#    self.xmpp.sendMessage(u, 'message', 
#                          mfrom="dogfood@im.partych.at", mtype='chat')


  
  def handle_probe(self, probe):
    logging.info('probe: %s', probe)
    return 

  def start(self) :
    self.xmpp.connect()
    self.xmpp.process()
