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
import urllib
import tornado

from functools import partial
from pymongo import Connection, ASCENDING, DESCENDING
from pymongo.objectid import ObjectId
import asyncmongo

#import argparse

#parser = argparse.ArgumentParser()
#parser.add_argument('db_host', default='localhost')
#parser.add_argument('db_port', type=int, default=27017)



try:
  import simplejson as json
except:
  import json
from collections import defaultdict, Counter, deque

from http import Stats

SUBDOMAIN = 'im'
MYDOMAIN = SUBDOMAIN + '.partych.at'

PARTYCHAPP_CONTROL = '__control@partychapp.appspotchat.com'
MY_CONTROL = '_control@' + SUBDOMAIN + '.partych.at'

STATUS = ''


PROXY_JID_PATTERN = '%s@' + SUBDOMAIN + '.partych.at/pcbot'
PROXY_BARE_JID_PATTERN = '%s@'+ SUBDOMAIN + '.partych.at/pcbot'
MY_CONTROL_FULL = PROXY_JID_PATTERN % '_control'

OVERRIDE_TIME = [None]

class State:
  UNKNOWN = 0
  PENDING = 1
  REJECTED = 2
  OK = 3

  OVERRIDE_TIME = None
  @staticmethod
  def time(*args, **kwargs):
    return time.time() if OVERRIDE_TIME[-1] is None else OVERRIDE_TIME[-1]
  
  @staticmethod
  def is_ok(state):
    return (state['in_state'] == State.OK) and (state['out_state'] == State.OK)

  @staticmethod
  def can_rerequest(state):
    return (State.time() - 60*5) > state['last_out_request']


class SavingDict(dict):
  def __setitem__(self, k,v):
    dict.__setitem__(self, k,v)
    StateManager.instance().update(self, k)
    


strip_resource = lambda x:str(x).split('/')[0].lower()
make_channel = lambda x:str(x).split('@')[0].lower()


def run_and_call_in_loop(call_queue, response=None):
  logging.debug('RESPONSE:%s' % response)
  if call_queue:
    args, kwargs = call_queue.popleft()
    oargs = [args[0]]
    if call_queue:
      oargs.append(partial(run_and_call_in_loop, call_queue))
    if response:
      oargs.append(response)
    oargs += args[1:]
    if not response:
      tornado.ioloop.IOLoop.instance().add_callback(partial(*oargs, **kwargs))
    else:
      tornado.ioloop.IOLoop.instance().add_callback(partial(*oargs, **kwargs))

class StateManager:
  _instance = None

  def log_message(self, channel, user):
#    Stats['channel_%s' % channel].add(1)
#    Stats['_total_outbound'].add(1)

    pass
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
  def Init(cls, host, port, coll):
    cls._host = host
    cls._port = port
    cls._coll = coll

  @classmethod
  def instance(cls):
    if cls._instance is None:
      cls._instance = cls()
    return cls._instance

  def iter_channel_users(self):
    for c, user_dict in self._channel_user_state.iteritems():
      for u, s in user_dict.iteritems():
        yield c, u, s
        
  def __init__(self):
    self._conn = Connection(self._host, self._port)
    self._db = self._conn[self._coll]
    self._state_table = self._db['state']
    self._state_table.create_index([('channel', ASCENDING), 
                                    ('user', DESCENDING)],
                                   unique=True)
    self._async_db = asyncmongo.Client(pool_id='mydb', 
                                       host=self._host, 
                                       port=self._port, 
                                       maxcached=10, 
                                       maxconnections=100, 
                                       dbname=self._coll)
    self._async_state_table = self._async_db['state']
    


  @staticmethod
  def _make_query(channel, user):
    q = {'channel' : channel,
         'user'    : strip_resource(user)}
    return q

  def get_all(self, channel, users):
    q = {'channel' : channel,
         'user'    : {'$in' : map(strip_resource, users)}}
    found = [SavingDict(x) for x in self._state_table.find(q)]
    users_found = set([x['user'] for x in found])
    missing_users = set(users) - users_found
    if missing_users:
      logging.info('could not find %d new users', len(missing_users))
      for u in missing_users:
        found.append(self.get(channel, u))
    return found
                  
  def get_all_async(self, NEXT, channel, users):
    q = {'channel' : channel,
         'user'    : {'$in' : map(strip_resource, users)}}
    self._async_state_table.find(q, callback=partial(self._get_all_async_response, NEXT, channel, users))    


  def _get_all_async_response(self, NEXT, channel, users, response, error):
    assert not error
    found = [SavingDict(x) for x in response]
    users_found = set([x['user'] for x in found])
    missing_users = set(users) - users_found
    if missing_users:
      logging.info('could not find %d new users', len(missing_users))
      for u in missing_users:
        found.append(self.get(channel, u))
    NEXT(found)
    
    

  def get_async(self, NEXT, channel, user):
    q = self._make_query(channel, user)
    self._async_state_table.find(q, limit=1, callback=partial(self._get_async_response, NEXT, channel, user))


  def _get_async_response(self, NEXT, channel, user, response, error):
    logging.debug('************')
    assert not error
    if response:
      logging.debug('returning response, %s', response)
      NEXT(SavingDict(response[0]))
      return
    else:
      this_state = self._make_query(channel, user)
      this_state['in_state'] = State.UNKNOWN
      this_state['out_state'] = State.UNKNOWN
      this_state['message_received'] = False
      this_state['first_time'] = False
      this_state['last_out_request'] = 0
      idno = self._state_table.insert(this_state)
      this_state['_id'] = idno
      this_state['first_time'] = True
      this_state = SavingDict(this_state)
      logging.debug('returning response 2')
      NEXT(this_state)
    
  def get(self, channel, user):
    q = self._make_query(channel, user)

    # assert '@' not in channel and '/' not in channel
    this_state = self._state_table.find_one(q)

    if this_state:
      return SavingDict(this_state)
    else:
      this_state = q
      this_state['in_state'] = State.UNKNOWN
      this_state['out_state'] = State.UNKNOWN
      this_state['message_received'] = False
      this_state['first_time'] = True
      this_state['last_out_request'] = 0
      self._state_table.insert(this_state)
      this_state = SavingDict(self._state_table.find_one(q))
      assert this_state
      self._state_table.update(q, {'$set' : {'first_time' : False}})
      return this_state

  def update(self, state, k):
    # todo: make this async.
    self._state_table.update({'_id' : state['_id']},
                             {'$set': {k:state[k]}})

  def update_message_received(self, channel, user):
    q = self._make_query(channel, user)
    if not self._state_table.find_one(q):
      self.get(channel, user)

    self._state_table.update(q, {'$set' : {'message_received' : True}})
    return self.get(channel, user)


  @classmethod
  def load(cls, filename):
    assert not cls._instance
    logging.error('failed to load any data')

def SAVE(*args, **kwargs):
  pass
def do_exit(sig, stack):
    raise SystemExit('Exiting')

def GetControlMessage(event):
    if str(event['to']) != MY_CONTROL:
      return None
    else:
      # TODO: check from, and check signature of message
      msg_str = str(event['body'])
      if msg_str.startswith('gzip:'):
        try:
          msg_str = zlib.decompress(msg_str[len('gzip:'):])
        except:
          open('/tmp/broken', 'wb').write(msg_str)
      return json.loads(msg_str)

class SimpleComponent:
  def __init__(self, jid, password, server, port, oh) :
    self._oh = oh
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
    self.xmpp.add_event_handler('message', self.message, threaded=True)
    self.xmpp.add_event_handler('CONTROL', self._handle_control_message, 
                                threaded=True)

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
                    outmsg,
                    states=None):
      states = StateManager.instance().get_all(from_channel, recipients) if not states else states
      rmsg = 0
      for state in states:
        if state['in_state'] != State.OK or state['out_state'] != State.OK:
          logging.debug('BAD_STATE        %s  --> %s in %s out %s', 
                       from_channel, state['user'], state['in_state'], state['out_state'])
          self._send_subscribe(from_channel, state['user'], state=state)
          self._send_subscribed(from_channel, state['user'], state=state)
        else:
          rmsg +=1
          StateManager.instance().log_message(from_channel, state['user'])
          logging.debug('sending a message %s', outmsg)
          self.xmpp.sendMessage(mto=state['user'],
                                mbody=outmsg,
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



      run_and_call_in_loop(deque([
        ((StateManager.instance().get_all_async, from_channel, recipients), {}),
        ((self._outbound_message_with_state, from_channel, from_jid, recipients, outmsg), {})
        ]))



  def _outbound_message_with_state(self, states, from_channel, from_jid, recipients, outmsg):
      self._send_message(from_channel, from_jid, recipients, outmsg, states=states)

  def _inbound_message(self, message):
    if message['type'] == 'error':
      try:
        Stats['_total_inbound_error'].add(1)
        logging.debug('error: %s' %  str(message))
      except:
        pass
      return

    ctl = GetControlMessage(message)
    if ctl:
      self._handle_control_message(ctl)
      return

    event = message
    to_str = str(event['to'])
    from_str = str(event['from'])
    channel = make_channel(to_str)
    user = from_str
    run_and_call_in_loop(deque([
        ((StateManager.instance().get_async, channel, user), {}),
        ((self._inbound_message_with_state, message), {})
        ]))

  def _inbound_message_with_state(self, state, event):
    # inbound message
    # echo
    Stats['_total_inbound'].add(1)
    msg_str = str(event['body'])
    to_str = str(event['to'])
    from_str = str(event['from'])
    payload = dict(state='old',
                     to_str=to_str,
                     from_str=from_str,
                     message_str=msg_str)
    logging.info('MESSAGE              control<--- %s <--- %s',
                 to_str, from_str)
    self._send_subscribe(make_channel(to_str), from_str, state=state)
    self._oh.post(url='https://partychapp.appspot.com/___control___',
                  params={'token' : 'tokendata',
                   'body'  : json.dumps(payload)})

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

  def _send_presence(self, channel, user, status=STATUS, state=None):
    
    logging.debug('PRESENCE                %s->%s', channel, user)
    self._dispatch_presence(pfrom=PROXY_JID_PATTERN % channel,
                            pto=strip_resource(user),
                            pstatus=status,
#                           pshow='dnd'
                            )

  def _send_subscribed(self, channel, user,force=False, state=None):
    assert state
    if force or state['in_state'] not in [
      State.OK, State.PENDING, State.REJECTED]:
      self._dispatch_presence(pfrom=PROXY_BARE_JID_PATTERN % channel,
                             pto=strip_resource(user), 
                             ptype='subscribed')
      state['in_state'] = State.OK
      self._send_presence(channel, user)
      logging.info('SUBSCRIBED                %s->%s', channel, user)
    else:
      state['in_state'] = State.OK
      logging.debug('subscribed not sent due to state')


  def _send_subscribe(self, channel, user, state = None):

    assert state
    if (state['out_state'] == State.PENDING and State.can_rerequest(state)): 
      logging.info('SUBSCRIBE reset pending out state for %s --> %s', channel, user)
      state['out_state'] = State.UNKNOWN

    if state['out_state'] in [
      State.OK, 
      State.PENDING, State.REJECTED]:
      logging.debug('NOT sending outbound subscribe request for user %s for channel %s (s=%s)',
                 user, channel, state['out_state'])
      return

    state['last_out_request'] = State.time()
    state['out_state'] = State.PENDING

    logging.info('SUBSCRIBE                %s->%s', channel, user)
    self._dispatch_presence(pfrom=PROXY_BARE_JID_PATTERN % channel,
                           pto=strip_resource(user), 
                           ptype='subscribe')

  def _got_subscribe(self, channel, user, state=None):
    assert state
    state['in_state'] = State.OK
    # something screwy is happening here
    
    self._send_subscribed(channel, user, force=True, state=state)
    self._send_subscribe(channel, user, state=state)


  def _got_subscribed(self, channel, user, state=None):
    assert state
    state['out_state'] = State.OK
    self._send_subscribe(channel, user, state=state)
    self._send_presence(channel, user)
    from_jid = '%s@%s/pcbot' % (channel, MYDOMAIN)
    if state['first_time']:
      self._send_message(channel, from_jid,
                         [user],
                         'Welcome to ' + channel)
      logging.info('sent welcome message for %s,%s', user, channel)
    else: 
      logging.info('did not send welcome message for %s,%s due to not first time', 
                   user, channel)
    
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
      run_and_call_in_loop(deque([
        ((StateManager.instance().get_async, channel, user), {}),
        ((self._generic_handler, s, event, channel, user), {})
        ]))

    except Exception as e:
      logging.error(e)
      return
    

  def _generic_handler(self, state, s, event, channel, user):
      logging.debug('state: %s, event: %s, cu:%s, %s', state, event, channel, user)
      if s == 'presence_available':
        # we silently ignore presence updates.
        return

      if s == 'subscribe':
        logging.info('SUBSCRIBE              %s<-%s', channel, user)
        self._got_subscribe(channel, user, state=state)
      elif s == 'subscribed':
        logging.info('SUBSCRIBED             %s<-%s', channel, user)
        self._got_subscribed(channel, user,state=state)
      elif s == 'unsubscribed':
        logging.info('UNSUBSCRIBED           %s<-%s', channel, user)
        state['in_state'] = State.UNKNOWN # TODO: rejected
        state['out_state'] = State.UNKNOWN # TODO: rejected
      elif s == 'unsubscribe':
        logging.info('UNSUBSCRIBE            %s<-%s', channel, user)
        state['in_state'] = State.UNKNOWN # TODO: rejected
        state['out_state'] = State.UNKNOWN # TODO: rejected

      elif s == 'probe':
        logging.debug('PROBE                  %s<-%s', channel, user)
        # if necessary
        self._send_subscribe(channel, user, state=state)
        self._send_subscribed(channel, user, state=state)
        #

        self._send_presence(channel, user, state=state)


    

  def start_session(self, *args, **kwargs):
    logging.info('started session')
    atexit.register(SAVE)
    # if we've sent a status update in the last three minutes, don't send another
    self._send_message('_control', MY_CONTROL_FULL,
                       [PARTYCHAPP_CONTROL],
                       'hi')
                       


  def start(self):
    StateManager.load('state.partychatproxy')
    self.xmpp.connect()
    self.xmpp.process()


