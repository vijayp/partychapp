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

  def can_send(self):
    return self._state in [State.OK, State.UNKNOWN]


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


def GetControlMessage(event):
    if str(event['to']) != MY_CONTROL:
      return None
    else:
#      assert str(event['from']).startswith(PARTYCHAPP_CONTROL)
      # TODO: check from, and check signature of message
      msg_str = str(event['body'])
      if msg_str.startswith('gzip:'):
	try:
          msg_str = zlib.decompress(msg_str[len('gzip:'):])
        except:
          open('/tmp/broken', 'wb').write(msg_str)
      return json.loads(msg_str)

class SimpleComponent:
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
    

  def _handle_control_message(self, ctl):
      outmsg = ctl.get('outmsg')
      recipients = ctl.get('recipients', [])

      from_channel = ctl.get('from_channel', '')
      assert from_channel and recipients
      assert '@' not in from_channel # TODO better validation
      from_jid = '%s@%s' % (from_channel, MYDOMAIN)
      rmsg = 0
      for r in recipients:
#        logging.info('trying recipient %s' % r)
        if (r, from_jid) not in self._state or self._state[(r, from_jid)].can_send():
          rmsg +=1
          self.xmpp.sendMessage(r,
                                outmsg,
                                mfrom=from_jid,
                                mtype='chat')
        else:
#          logging.info('failed recipient %s' % r)
          pass
      logging.info('sent message to %s (%d out of %d recipients)',
                   from_jid,
                   rmsg,
                   len(recipients))
        

  def _inbound_message(self, message):
    ctl = GetControlMessage(message)
    if ctl:
      self._handle_control_message(ctl)
      return
    # inbound message
    # echo

                           
    event = message
    to_str = str(event['to'])
    msg_str = str(event['body'])
    from_str = str(event['from'])
    payload = dict(state='old',
                     to_str=to_str,
                     from_str=from_str,
                     message_str=msg_str)
    logging.info('sending message to partychapp control for (%s, %s)',
                 to_str, from_str)

    self.xmpp.sendMessage(PARTYCHAPP_CONTROL,
                            json.dumps(payload),
                            mfrom=MY_CONTROL,
                            mtype='chat')
    self.xmpp.sendPresence(pto=event['from'], pfrom=event['to'], pstatus=STATUS)

  def message(self, message):

    if message['type'] == 'error':
      logging.error('ERROR: %s', message)
      from_person = str(message['from'])
      channel_id = str(message['to'])
      if self._state[(from_person, channel_id)].should_request():
        logging.info('requesting subscription for (%s,%s)', from_person, channel_id)
        self._state[(from_person, channel_id)].request_pending()
        logging.info('set state to pending for %s' % from_person)
        self.xmpp.sendPresence(pto=message['from'], pfrom=message['to'],
                               ptype='subscribe')

    else:
      self._inbound_message(message)

  def generic_handler(self, s, event):
    try:
      if not event:
        return
      if s in ['roster_update'] or event['type'] == 'probe':#, 'pre
        logging.info('sending presence to %s from %s' , event['to'], event['from'])
        self.xmpp.sendPresence(pto=event['from'], pfrom=event['to'], pstatus=STATUS)
    except:
      return

  def send_unsubscribe(self, u, f):
    self.xmpp.sendPresence(pto=u, pfrom=f, 
                           ptype="presence_unsubscribed")
    

  def start_session(self, *args, **kwargs):
    logging.info('started session')
    logging.info('trying to probe vijayp@gmail.com for dogfood')
    u = 'vijayp@gmail.com'
    self.xmpp.sendPresence(pto=u, pfrom=MY_CONTROL,
                           pstatus=STATUS)
    u = PARTYCHAPP_CONTROL
    self.xmpp.sendPresence(pto=u, pfrom=MY_CONTROL,
                           pstatus=STATUS)


  def handle_probe(self, probe):
    logging.info('probe: %s', probe)
    return 

  def start(self) :
    self.xmpp.connect()
    self.xmpp.process()
