import zlib
import atexit
import tempfile
import cPickle
import sys
import sleekxmpp.componentxmpp
import logging
import os
try:
  import simplejson as json
except:
  import json
import json
import zlib
from collections import defaultdict

MYDOMAIN = 'im.partych.at'

PARTYCHAPP_CONTROL = '__control@partychapp.appspotchat.com'
MY_CONTROL = '_control@im.partych.at'
STATUS = 'replacing app engine since 2011'


PROXY_JID_PATTERN = '%s@im.partych.at/pcbot'
PROXY_BARE_JID_PATTERN = '%s@im.partych.at/pcbot'

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

  def is_subscribed(self):
    return self._state == State.OK

  def is_unknown(self):
    return self._state == State.UNKNOWN

  def is_pending(self):
    return self._state == State.PENDING


strip_resource = lambda x:str(x).split('/')[0]
class StateManager:
  _instance = None

  @classmethod
  def instance(cls):
    if StateManager._instance is None:
      StateManager._instance = StateManager()
    return StateManager._instance

  def iter_channel_users(self):
    for c, user_dict in self._channel_user_state.iteritems():
      for u, s in user_dict.iteritems():
        yield c, u, s
        
  def __init__(self):
    self._channel_user_state = defaultdict(lambda:defaultdict(State))

  def get(self, channel, user):
    # assert '@' not in channel and '/' not in channel
    return self._channel_user_state[channel][strip_resource(user)]

  def set(self, channel, user, state):
    # assert '@' not in channel and '/' not in channel
    self._channel_user_state[channel][strip_resource(user)] = state

  @classmethod
  def save(cls, filename):
    logging.error('dumping state to %s', filename)
    (fd, fn) = tempfile.mkstemp()
    fd = os.fdopen(fd, 'w')
    logging.error('tempfile: %s', fn)
    out = [x for x in cls._instance.iter_channel_users()]
    cPickle.dump(out, fd)
    fd.close()
    os.rename(fn, filename)
    logging.error('dump+rename done %s', filename)

  @classmethod
  def load(cls, filename):
    assert not cls._instance
    cls.instance()
    try:
      logging.error('loading state from %s', filename)
      in_dat = cPickle.load(open(filename, 'rb'))
      for (c, u, s) in in_dat:
        cls.instance().set(c,u, s)
        logging.info('%s,%s = %s',
                     c,u,s)
      logging.error('loaded %s data records', len(in_dat))
             
    except:
      logging.error('failed to load any data')



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
    self.xmpp.auto_authorize = None
    self.xmpp.auto_subscribe = None
    self.xmpp.add_event_handler('session_start', self.start_session)
    self.xmpp.add_event_handler('message', self.message)

    for s in ['presence_subscribe', 
              'presence_subscribed', 
              'presence_unsubscribe', 
              'presence_unsubscribed',
              'presence_probe',
              'presence_available',
#              'presence_error',
#              'got_online',
#              'got_offline',
#              'changed_status',
#              'changed_subscription',
#              'message',
#              'message_form',
#              'message_xform',
#              'presence_form',
#              'roster_update',
#              'sent_presence',
              ]:
      self.xmpp.add_event_handler(s, lambda event: self.generic_handler(s, event))
    

  def _handle_control_message(self, ctl):
      outmsg = ctl.get('outmsg')
      recipients = ctl.get('recipients', [])

      from_channel = ctl.get('from_channel', '')
      assert from_channel and recipients
      assert '@' not in from_channel # TODO better validation
      from_jid = '%s@%s/pcbot' % (from_channel, MYDOMAIN)
      rmsg = 0
                             

      for r in recipients:
        state = StateManager.instance().get(from_channel, r)
        if not state.is_subscribed():
          if state.is_unknown():
            self._send_subscribe(from_channel, r)
        else:
          rmsg +=1
          self.xmpp.sendMessage(r,
                                outmsg,
                                mfrom=from_jid,
                                mtype='chat')
      logging.info('sent message to %s (%d out of %d recipients)',
                   from_jid,
                   rmsg,
                   len(recipients))
        

  def _inbound_message(self, message):
    if message['type'] == 'error':
      return

    self._handle_control_message(dict(outmsg=message['body'],
                                      recipients=[str(message['from']).split('/')[0]],
                                      from_channel=str(message['to']).split('@')[0]
                                      )
                                 )

    return

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



  def _send_presence(self, channel, user, status=STATUS):
    logging.info('presence --> %s,%s' , channel, user)
 
    self.xmpp.sendPresence(pfrom=PROXY_JID_PATTERN % channel,
                           pto=strip_resource(user),
                           pstatus=status,
#                           pshow='dnd'
                           )

  def _send_subscribed(self, channel, user):
    logging.info('sending outbound subscribed from user %s for channel %s',
                 user, channel)
    self.xmpp.sendPresence(pfrom=PROXY_BARE_JID_PATTERN % channel,
                           pto=strip_resource(user), 
                           ptype='subscribed')
    #state = StateManager.instance().get(channel, user)._state = State.OK

  def _send_subscribe(self, channel, user):
    state = StateManager.instance().get(channel, user)
    if not state.is_unknown():
      logging.info('NOT sending outbound subscribe request for user %s for channel %s',
                 user, channel)
      return
    else:
      logging.info('sending outbound subscribe request for user %s for channel %s',
                   user, channel)
    self.xmpp.sendPresence(pfrom=PROXY_BARE_JID_PATTERN % channel,
                           pto=strip_resource(user), 
                           ptype='subscribe')
    self._send_presence(channel, user)
    state = StateManager.instance().get(channel, user)._state = State.PENDING

  def _got_subscribed(self, channel, user):
    logging.info('got inbound subscribe request from user %s for channel %s',
                 user, channel)
    state = StateManager.instance().get(channel, user)._state = State.OK
    self._send_subscribed(channel, user)
    self._send_presence(channel, user)
    logging.info('would have sent welcome message here')
    
  def message(self, message):
#    if str(message['from']).find('vijayp') == -1:
#      return
    self._inbound_message(message)
    logging.info(message)
    return


    if message['type'] == 'error':

      from_person = str(message['from'])
      channel_id = str(message['to'])
      if self._state[(from_person, channel_id)].should_request():
        logging.info('requesting subscription for (%s,%s)', from_person, channel_id)
        self._state[(from_person, channel_id)].request_pending()
        logging.info('set state to pending for %s' % from_person)
        self.xmpp.sendPresence(pto=message['from'], pfrom=message['to'],
                               ptype='subscribed')
        self.xmpp.sendPresence(pto=message['from'], pfrom=message['to'],
                               pstatus='status')
        self.xmpp.sendPresence(pto=message['from'], pfrom=message['to'],
                               ptype='subscribe')

    else:
      self._inbound_message(message)

  def generic_handler(self, s, event):
    s = event['type'] if 'type' in event else None
    logging.info('%s,%s, %s', s, event,'type' in event)
#    if str(event['from']).find('vijayp') == -1:
#      return
#    return
    try:
      if not event:
        return
      user = event['from']
      channel = str(event['to']).split('@')[0]
      if 'type' not in event:
        # this is a presence update, so we know we're ok.
        if StateManager.instance().get(channel,user)._state != State.OK:
          logging.info('setting %s,%s to OK because of inbound presence stanza',
                       channel, user)
          self._send_presence(channel, user)
          StateManager.instance().get(channel,user)._state = State.OK

        

      if s == 'subscribe':
        self._send_subscribed(channel, user)
        self._send_subscribe(channel, user)
      elif s == 'subscribed':
        self._got_subscribed(channel, user)
      elif s == 'unsubscribed':
        pass
      elif s == 'unsubscribe':
        pass

      if event['type'] == 'probe':
        self._send_presence(channel, user)

    except Exception as e:
      logging.error(e)
      return

    

  def start_session(self, *args, **kwargs):
    logging.info('started session')
    for c,u,_ in StateManager.instance().iter_channel_users():
      # TODO: execute this in the background somehow. This can take a long time.
      self._send_presence(c,u)


  def start(self):
    StateManager.load('state.partychatproxy')
    atexit.register(StateManager.save, 'state.partychatproxy')
    self.xmpp.connect()
    self.xmpp.process()
