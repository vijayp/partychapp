import unittest
from unittest import TestCase
import SimpleComponent
from SimpleComponent import *
import time
from pymongo import Connection
from collections import deque

import tornado.ioloop


def run_in_loop(*args, **kwargs):
  actual_callback = partial(*args, **kwargs)
  def my_callback():
    actual_callback()
    tornado.ioloop.IOLoop.instance().add_timeout(time.time() + 0.5, loop_run_done, )

  tornado.ioloop.IOLoop.instance().add_callback(my_callback)
  tornado.ioloop.IOLoop.instance().start()



def loop_run_done(*args, **kwargs):
  tornado.ioloop.IOLoop.instance().stop()



class VirginStateTest(TestCase):
  def setUp(self):
    
    StateManager.Init('localhost', 27017, 
                      'test_db')
    self._sm = StateManager().instance()
    self._sm._state_table.drop()
    print 'in setup'

    OVERRIDE_TIME.append(1)


  def tearDown(self):
    print 'tearing down'
    self._sm._state_table.drop()
  
  def test_sets(self):
    state = self._sm.get('mychannel', 'user@gmail.com')
    del state['_id']
    OK = {'first_time' : True,
          'message_received' : False,
          'user' : 'user@gmail.com',
          'channel' : 'mychannel',
          'out_state': 0, 'last_out_request': 0, 'in_state': 0}
    print OK, state
    assert OK == state


    state = self._sm.get('mychannel', 'user@gmail.com')
    state['in_state'] = State.PENDING


    state = self._sm.get('mychannel', 'user@gmail.com')
    del state['_id']
    OK['first_time'] = False
    OK['in_state'] = 1

    assert OK == state, (OK, state)

    state = self._sm.get('mychannel', 'user@gmail.com/node')
    del state['_id']
    assert OK == state


    state = self._sm.get('mychannel', 'user@gmail.com')
    state['out_state'] =  State.PENDING
    state = self._sm.get('mychannel', 'user@gmail.com')
    del state['_id']
    OK['out_state'] = 1
    assert OK == state

    # unkonwn user
#    self.assertRaises(Exception,
#                      self._sm.set_instate, 'mychannel', 'unknown@gmail.com', 
#                         State.PENDING)


  def test_messages(self):
    class TSimpleComponent(SimpleComponent):
      def __init__(self):
        pass

    class FakeXMPP(TestCase):
      def __init__(self, log=False):
        self._queue = deque()
        self._log = log

      def send(self, *args, **kwargs):
        pass

      def make_presence(self, *args, **kwargs):
       try:
        logging.info('(%d) RECEIVED %s', len(self._queue),
                     repr( kwargs))
        ok = self._queue.popleft()
        assert ok == kwargs, (kwargs, ok)
       except Exception, e:
         if self._log:
           logging.info('MISSING %s', repr( kwargs))
           logging.error(e)
         else:
           raise
       return FakeXMPP()

      def sendMessage(self, *args, **kwargs):
       try:
        logging.info('(%d) RECEIVED %s', len(self._queue),
                     repr( kwargs))
        ok = self._queue.popleft()
        assert ok == kwargs, (kwargs, ok)
       except Exception, e:
         if self._log:
           logging.info('MISSING %s', repr( kwargs))
           logging.error(e)
         else:
           raise

      def post(self, *args, **kwargs):
        self.sendMessage(*args, **kwargs)
  
      
    proxy = TSimpleComponent()
    
    proxy.xmpp = FakeXMPP()
    proxy._oh = proxy.xmpp

    # test inbound control message
    proxy.xmpp._queue.append({'pto': 'u1@gmail.com', 'pfrom': 'test_channel@im.partych.at/pcbot', 'ptype': 'subscribe'})
    proxy.xmpp._queue.append({'pto': 'u1@gmail.com', 'pfrom': 'test_channel@im.partych.at/pcbot', 'ptype': 'subscribed'})
    proxy.xmpp._queue.append({'pto': 'u1@gmail.com', 'pstatus': STATUS, 'pfrom': 'test_channel@im.partych.at/pcbot'})
    proxy.xmpp._queue.append({'pto': 'u2@gmail.com', 'pfrom': 'test_channel@im.partych.at/pcbot', 'ptype': 'subscribe'})
    proxy.xmpp._queue.append({'pto': 'u2@gmail.com', 'pfrom': 'test_channel@im.partych.at/pcbot', 'ptype': 'subscribed'})
    proxy.xmpp._queue.append({'pto': 'u2@gmail.com', 'pstatus': STATUS, 'pfrom': 'test_channel@im.partych.at/pcbot'})

    logging.info('=================== testing first message')
    run_in_loop(proxy._handle_control_message, 
      dict(outmsg='test message',
           recipients=['u1@gmail.com', 'u2@gmail.com',],
           from_channel='test_channel'))


    #second message from same person
    #no time has passed, so new subscribe request should be sent.
    logging.info('=================== testing second message')
    run_in_loop(proxy._handle_control_message, 
      dict(outmsg='test message',
           recipients=['u1@gmail.com', 'u2@gmail.com',],
           from_channel='test_channel'))
    assert not proxy.xmpp._queue
    OVERRIDE_TIME.append(100000)

    #TODO: maybe we should add subscribed as well.


    proxy.xmpp._queue.append({'pto': 'u1@gmail.com', 'pfrom': 'test_channel@im.partych.at/pcbot', 'ptype': 'subscribe'})
    proxy.xmpp._queue.append({'pto': 'u2@gmail.com', 'pfrom': 'test_channel@im.partych.at/pcbot', 'ptype': 'subscribe'})
    logging.info('=================== testing third message, %s', len(proxy.xmpp._queue))
    run_in_loop(proxy._handle_control_message,
      dict(outmsg='test message',
           recipients=['u1@gmail.com', 'u2@gmail.com',],
           from_channel='test_channel'))
    assert not proxy.xmpp._queue

    logging.info('=================== testing inbound subscribed, %s', len(proxy.xmpp._queue))
    proxy.xmpp._queue.append({'pto': 'u1@gmail.com', 'pstatus': STATUS, 'pfrom': 'test_channel@im.partych.at/pcbot'})
    proxy.xmpp._queue.append({'mbody': 'Welcome to test_channel', 'mtype': 'chat', 'mfrom': 'test_channel@im.partych.at/pcbot', 'mto': u'u1@gmail.com'})

    channel,user = 'test_channel', 'u1@gmail.com'
    state = StateManager.instance().get(channel, user)


    assert state
    run_in_loop(proxy._got_subscribed,'test_channel', 'u1@gmail.com', state=state)

    proxy.xmpp._queue.append({'pto': 'random123@gmail.com', 'pstatus': STATUS, 'pfrom': 'test_channel@im.partych.at/pcbot'})
    proxy.xmpp._queue.append({'pto': 'random123@gmail.com', 'pfrom': 'test_channel@im.partych.at/pcbot', 'ptype': 'subscribed'})
    proxy.xmpp._queue.append({'pto': 'random123@gmail.com', 'pstatus': STATUS, 'pfrom': 'test_channel@im.partych.at/pcbot'})

    channel,user = 'test_channel', 'random123@gmail.com'
    state = StateManager.instance().get(channel, user)
    proxy._got_subscribed('test_channel', 'random123@gmail.com', state=state)

    proxy.xmpp._queue.append({'pto': 'random1234@gmail.com', 'pfrom': 'test_channel@im.partych.at/pcbot', 'ptype': 'subscribed'})
    proxy.xmpp._queue.append({'pto': 'random1234@gmail.com', 'pstatus': STATUS, 'pfrom': 'test_channel@im.partych.at/pcbot'})
    proxy.xmpp._queue.append({'pto': 'random1234@gmail.com', 'pfrom': 'test_channel@im.partych.at/pcbot', 'ptype': 'subscribe'})
    channel,user = 'test_channel', 'random1234@gmail.com'
    state = StateManager.instance().get(channel, user)
    run_in_loop(proxy._got_subscribe,'test_channel', 'random1234@gmail.com', state = state)





    proxy.xmpp._queue.append({'pto': 'u1@gmail.com', 'pstatus': STATUS, 'pfrom': 'test_channel@im.partych.at/pcbot'})
    proxy.xmpp._queue.append({'mbody': 'Welcome to test_channel', 'mtype': 'chat', 'mfrom': 'test_channel@im.partych.at/pcbot', 'mto': u'u1@gmail.com'})

    logging.info('=================== testing inbound subscribe, %s', len(proxy.xmpp._queue))


    channel,user = 'test_channel', 'u1@gmail.com'
    state = StateManager.instance().get(channel, user)
    run_in_loop(proxy._got_subscribed,'test_channel', 'u1@gmail.com', state=state)



    proxy.xmpp._queue.append( {'url': 'https://partychapp.appspot.com/___control___', 'params': {'body': '{"from_str": "u1@gmail.com", "to_str": "test_channel@im.partych.at", "state": "old", "message_str": "test inbound message"}', 'token': 'tokendata'}})
    logging.info('=================== testing inbound message, %s', len(proxy.xmpp._queue))

    run_in_loop(proxy._inbound_message,
            {'to' : 'test_channel@im.partych.at',
             'from' : 'u1@gmail.com',
             'body' : 'test inbound message',
             'type' : 'not_error'})
    
    assert not proxy.xmpp._queue
if __name__ == '__main__':
  import logging
  ch = logging.StreamHandler()
  ch.setLevel(logging.DEBUG)
  formatter = logging.Formatter(
      "%(levelname).1s%(asctime)s %(filename)s:%(lineno)d] %(message)s",
      datefmt='%Y%m%d:%H%M%S')
  ch.setFormatter(formatter)
  for L in [logging.getLogger(''), logging.getLogger()]:
    L.setLevel(logging.DEBUG)
    L.addHandler(ch)


  unittest.main()
