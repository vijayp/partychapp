import zlib
import time
import atexit
import tempfile
import cPickle
import sys
import sleekxmpp.componentxmpp
import logging
import signal
import os
try:
  import simplejson as json
except:
  import json
import json
import zlib
from collections import defaultdict, Counter

SUBDOMAIN = 'im'

MYDOMAIN = SUBDOMAIN + '.partych.at'

PARTYCHAPP_CONTROL = '__control@partychapp.appspotchat.com'
MY_CONTROL = '_control@' + SUBDOMAIN + '.partych.at'

STATUS = 'replacing app engine since 2011'


PROXY_JID_PATTERN = '%s@' + SUBDOMAIN + '.partych.at/pcbot'
PROXY_BARE_JID_PATTERN = '%s@'+ SUBDOMAIN + '.partych.at/pcbot'
MY_CONTROL_FULL = PROXY_JID_PATTERN % '_control'
import time




class State:
  UNKNOWN = 0
  PENDING = 1
  REJECTED = 2
  OK = 3
  def __init__(self):
    self.in_state = State.UNKNOWN
    self.out_state = State.UNKNOWN
    self._timestamp = time.time()

  def is_ok(self):
    return (self.in_state == State.OK) and (self.out_state == State.OK)

  def update_timestamp(self):
    self._timestamp=time.time()


strip_resource = lambda x:str(x).split('/')[0].lower()
make_channel = lambda x:str(x).split('@')[0].lower()
class StateManager:
  _instance = None

  def log_message(self, channel, user):
    self._counters['channel'][channel] += 1
#    self._counters['user'][user] += 1
  
  def counters_as_tuples(self):
    for t, keycount in self._counters.items():
      for k,v in keycount.items():
        yield t,k,v
    

  def num_channels(self):
    return len(self._channel_user_state)

  def num_ok_bad_total_users(self):
    ok = 0
    bad = 0
    for c, u, s in self.iter_channel_users():
      if s.is_ok():
        ok += 1
      else:
        bad += 1
    return ok, bad, ok+bad

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
    self._counters = defaultdict(Counter)

  def get(self, channel, user):
    # assert '@' not in channel and '/' not in channel
    return self._channel_user_state[channel][strip_resource(user)]

  def set(self, channel, user, state):
    # assert '@' not in channel and '/' not in channel
    self._channel_user_state[channel][strip_resource(user)] = state

  @classmethod
  def save(cls, filename):
    logging.error('dumping state to %s', filename)
    (fd, fn) = tempfile.mkstemp(dir='.') # same dir as final filename
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
        logging.info('%s,%s = (in=%s, out=%s)',
                     c,u,s.in_state, s.out_state)
      logging.error('loaded %s data records', len(in_dat))
             
    except:
      logging.error('failed to load any data')

def SAVE(*args, **kwargs):
  logging.info('SAVING...')
  StateManager.save('state.partychatproxy')
  StateManager.save('state.partychatproxy.' + str(time.time()))

def do_exit(sig, stack):
    SAVE()
    raise SystemExit('Exiting')

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



    self.xmpp.del_event_handler('presence_available',
                           self.xmpp._handle_available)
    self.xmpp.del_event_handler('presence_dnd',
                           self.xmpp._handle_available)
    self.xmpp.del_event_handler('presence_xa',
                           self.xmpp._handle_available)
    self.xmpp.del_event_handler('presence_chat',
                           self.xmpp._handle_available)
    self.xmpp.del_event_handler('presence_away',
                           self.xmpp._handle_available)
    self.xmpp.del_event_handler('presence_unavailable',
                           self.xmpp._handle_unavailable)
    self.xmpp.del_event_handler('presence_subscribe',
                           self.xmpp._handle_subscribe)
    self.xmpp.del_event_handler('presence_subscribed',
                           self.xmpp._handle_subscribed)
    self.xmpp.del_event_handler('presence_unsubscribe',
                           self.xmpp._handle_unsubscribe)
    self.xmpp.del_event_handler('presence_unsubscribed',
                           self.xmpp._handle_unsubscribed)
    self.xmpp.del_event_handler('roster_subscription_request',
                           self.xmpp._handle_new_subscription)

    self.xmpp.del_event_handler('presence_probe', 
                                self.xmpp._handle_probe)







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
    

  def _send_message(self, 
                    from_channel, 
                    from_jid,
                    recipients,
                    outmsg):

      rmsg = 0
      for r in recipients:
        state = StateManager.instance().get(from_channel, r)
        if state.in_state != State.OK or state.out_state != State.OK:
          logging.debug('BAD_STATE        %s  --> %s in %s out %s', 
                       from_channel, r, state.in_state, state.out_state)
          self._send_subscribe(from_channel, r)
          self._send_subscribed(from_channel, r)
        else:
          rmsg +=1
          StateManager.instance().log_message(from_channel, r)
          self.xmpp.sendMessage(r,
                                outmsg,
                                mfrom=from_jid,
                                mtype='chat')
#          self._send_presence(from_channel, r)
      logging.info('MESSAGE               %s --> (%d out of %d)',
                   from_jid,
                   rmsg,
                   len(recipients))
                    

  def _handle_control_message(self, ctl):
      outmsg = ctl.get('outmsg')
      recipients = ctl.get('recipients', [])

      from_channel = ctl.get('from_channel', '')
      assert from_channel and recipients
      assert '@' not in from_channel # TODO better validation
      from_jid = '%s@%s/pcbot' % (from_channel, MYDOMAIN)
      self._send_message(from_channel, from_jid, recipients, outmsg)

  def _inbound_message(self, message):
    if message['type'] == 'error':
      try:
        logging.info('error: %s' %  str(message))
      except:
        pass
      return
#    try:
#      if not str(event['to']).split('@')[1].lower().startswith(SUBDOMAIN):
#        logging.info('invalid inbound domain')
#        return
#    except:
#      pass

    
    if 1==0:
      self._handle_control_message(dict(outmsg=message['body'],
                                        recipients=[str(message['from']).split('/')[0]],
                                        from_channel=str(message['to']).split('@')[0]
                                        )
                                   )

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
    logging.info('MESSAGE              control<--- %s <--- %s',
                 to_str, from_str)
    self._send_subscribe(make_channel(to_str), from_str)
    self._send_message('_control',
                       MY_CONTROL_FULL,
                       [PARTYCHAPP_CONTROL],
                       json.dumps(payload))

  def _dispatch_presence(self, *args, **kwargs):
        """
        Create, initialize, and send a Presence stanza.

        Arguments:
            pshow     -- The presence's show value.
            pstatus   -- The presence's status message.
            ppriority -- This connections' priority.
            ptype     -- The type of presence, such as 'subscribe'.
            pnick     -- Optional nickname of the presence's sender.
        """
        p = self.xmpp.make_presence(**kwargs)
        p.send()
        del p

  def _send_presence(self, channel, user, status=STATUS):
    
    logging.debug('PRESENCE                %s->%s', channel, user)
    StateManager.instance().get(channel, user).update_timestamp()
    self.xmpp.sendPresence(pfrom=PROXY_JID_PATTERN % channel,
                           pto=strip_resource(user),
                           pstatus=status,
#                           pshow='dnd'
                           )

  def _send_subscribed(self, channel, user,force=False):

    if force or StateManager.instance().get(channel, user).in_state not in [
      State.OK, State.PENDING, State.REJECTED]:
      self._dispatch_presence(pfrom=PROXY_BARE_JID_PATTERN % channel,
                             pto=strip_resource(user), 
                             ptype='subscribed')
      state = StateManager.instance().get(channel, user).in_state = State.OK
      self._send_presence(channel, user)
      logging.info('SUBSCRIBED                %s->%s', channel, user)
    else:
      state = StateManager.instance().get(channel, user).in_state = State.OK
      logging.debug('subscribed not sent due to state')


  def _send_subscribe(self, channel, user):
    if StateManager.instance().get(channel, user).out_state in [
      State.OK, 
      State.PENDING, State.REJECTED]:
      logging.debug('NOT sending outbound subscribe request for user %s for channel %s',
                 user, channel)
      return


    state = StateManager.instance().get(channel, user).out_state = State.PENDING
    logging.info('SUBSCRIBE                %s->%s', channel, user)
    self._dispatch_presence(pfrom=PROXY_BARE_JID_PATTERN % channel,
                           pto=strip_resource(user), 
                           ptype='subscribe')

  def _got_subscribe(self, channel, user):
    StateManager.instance().get(channel, user).in_state = State.OK
    # something screwy is happening here
    
    #StateManager.instance().get(channel, user).out_state = State.UNKNOWN

    self._send_subscribed(channel, user, force=True)
    self._send_subscribe(channel, user)


  def _got_subscribed(self, channel, user):
    StateManager.instance().get(channel, user).out_state = State.OK
    self._send_subscribe(channel, user)
    self._send_presence(channel, user)
    from_jid = '%s@%s/pcbot' % (channel, MYDOMAIN)
    self._send_message(channel, from_jid,
                       [user],
                       'Welcome to ' + channel)
    logging.info('sent welcome message')
    
  def message(self, message):
#    if str(message['from']).find('vijayp') == -1:
#      return
    self._inbound_message(message)
    logging.debug(message)
    return



  def generic_handler(self, s, event):
 #   logging.info(s)
    s = None
    try:
      s = event['type']
    except:
      logging.error(s)
      return
      pass

#    logging.info('%s,%s', s, event)
#    if str(event['from']).find('vijayp') == -1:
#      return
#    return
    try:
      if not event:
        return
      user = event['from']
      channel = str(event['to']).split('@')[0]
      if s == 'presence_available':
        # this is a presence update, so we know we're ok.
        if StateManager.instance().get(channel,user)._state == State.UNKNOWN:
          logging.info('not setting %s,%s to OK because of inbound presence stanza',
                       channel, user)
#          self._send_presence(channel, user)
#          StateManager.instance().get(channel,user)._state = State.OK

        return

      if s == 'subscribe':
        logging.info('SUBSCRIBE              %s<-%s', channel, user)
        self._got_subscribe(channel, user)
      elif s == 'subscribed':
        logging.info('SUBSCRIBED             %s<-%s', channel, user)
        self._got_subscribed(channel, user)
      elif s == 'unsubscribed':
        logging.info('UNSUBSCRIBED           %s<-%s', channel, user)
        StateManager.instance().get(channel, user).out_state = State.UNKNOWN # TODO: rejected
        StateManager.instance().get(channel, user).in_state = State.UNKNOWN # TODO: rejected
      elif s == 'unsubscribe':
        logging.info('UNSUBSCRIBE            %s<-%s', channel, user)
        StateManager.instance().get(channel, user).out_state = State.UNKNOWN # TODO: rejected
        StateManager.instance().get(channel, user).in_state = State.UNKNOWN # TODO: rejected

      elif s == 'probe':
        logging.info('PROBE                  %s<-%s', channel, user)
        # if necessary
        self._send_subscribe(channel, user)
        self._send_subscribed(channel, user)
        #

        self._send_presence(channel, user)

    except Exception as e:
      logging.error(e)
      return

    

  def start_session(self, *args, **kwargs):
    logging.info('started session')
    atexit.register(SAVE)
    # if we've sent a status update in the last three minutes, don't send another
    CUTOFF= time.time() - 60*3
    for c,u,s in StateManager.instance().iter_channel_users():
      # TODO: execute this in the background somehow. This can take a long time.
      if s.in_state == State.OK and s.out_state == State.OK:
        if s._timestamp < CUTOFF:
          self._send_presence(c,u)

    
    ##
    self._send_message('_control', MY_CONTROL_FULL,
                       [PARTYCHAPP_CONTROL],
                       'hi')
                       


  def start(self):
    StateManager.load('state.partychatproxy')
    self.xmpp.connect()
    self.xmpp.process()


