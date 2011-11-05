import sys
sys.path.append("../3rdParty")
import sleekxmpp.componentxmpp
import logging
import simplejson as json

MYDOMAIN = 'im.partych.at'

PARTYCHAPP_CONTROL = '__control@partychapp.appspotchat.com'
#PARTYCHAPP_CONTROL = 'newpc_test@partychapp.appspotchat.com'
MY_CONTROL = '_control@im.partych.at'
STATUS = 'replacing app engine since 2011'
class SimpleComponent:
  @staticmethod
  def GetControlMessage(event):
    
    if str(event['to']) != MY_CONTROL:
      logging.info('to is <%s> but not <_control@im.partych.at>', str(event['to']))
      return None
    else:
      assert str(event['from']).startswith(PARTYCHAPP_CONTROL)
      # TODO: check from, and check signature of message
      msg_str = str(event['body'])
      logging.info('decoding <%s>', msg_str)
      return json.loads(msg_str)

    
  def __init__(self, jid, password, server, port, backend) :
    
    self.xmpp = sleekxmpp.componentxmpp.ComponentXMPP(jid, password, server, port)
    self.xmpp.auto_authorize = True
    self.xmpp.auto_subscribe = True
    for event in ["message"]:
      self.xmpp.add_event_handler(event, self.handleIncomingXMPPEvent)
    self.xmpp.add_event_handler('session_start', self.sessionStart)

    self._jid_resource_map = {}

  def sessionStart(self, *args, **kwargs):
    logging.info("session begun. Telling control channel I'm alive")
#    self.xmpp.sendPresence(pto=PARTYCHAPP_CONTROL, pfrom=MY_CONTROL,
#                             pstatus="presence probe",
#                             ptype="subscribed")
    self.xmpp.sendPresence(pto=PARTYCHAPP_CONTROL, pfrom=MY_CONTROL,
                             pstatus="presence probe")

    logging.info("I don't store rosters, so asking server for help")
    self.xmpp.sendMessage(PARTYCHAPP_CONTROL,
                          json.dumps(dict(state='new')),
                          mfrom=MY_CONTROL,
                          mtype='chat')
    


  def handle_probe(self, presence):
    sender = presence['from']
    if presence['type'] == 'unavailable':
      logging.info('%s has gone offline', sender)
    else:
      logging.info('%s has come online', sender)

    # Populate the presence reply with the agent's current status.
    self.sendPresence(pto=sender, pstatus="Busy studying XMPP")


  def handleIncomingXMPPEvent(self, event) :
    logging.debug('got xmpp incoming event for %s', event)
    
    logging.info('from: %s', event['from'])
    logging.info('to: %s', event['to'])

    if event['type'] == 'error':
      logging.error('GOT UNKNOWN ERROR FOR %s' % event)
      return

#    msgLocations = {sleekxmpp.stanza.presence.Presence: "status",
#                    sleekxmpp.stanza.message.Message: "body"}
#    assert type(event) == 
    message = event['body']
    logging.info('message: %s' % message)

    
    ctl = self.GetControlMessage(event)
    if ctl:
      outmsg = ctl.get('outmsg')
      recipients = ctl.get('recipients', [])
      from_channel = ctl.get('from_channel', '')
      assert from_channel and recipients
      assert '@' not in from_channel # TODO better validation
      from_jid = '%s@%s' % (from_channel, MYDOMAIN)

      debug_message = 'sending message <%s> to recipients[%s] from %s' % (
        outmsg, 
        ','.join(recipients),
        from_jid)
      logging.info(debug_message)
      #      self.xmpp.sendMessage(event['from'], debug_message, mfrom=from_jid)
      for rec in recipients:
        if not self.xmpp.roster[from_jid][rec].resources:
          logging.info('sending presence subscription')

          self.xmpp.sendPresenceSubscription(pfrom=from_jid,
                                             ptype='subscribe',
                                             pto=rec)
#          logging.info('sending presence subscribed')
#          self.xmpp.sendPresence(pto=rec, pfrom=from_jid,
#                                 pstatus=STATUS,
#                                 ptype="subscribed")
#          logging.info('sending presence status')
          self.xmpp.sendPresence(pto=rec, pfrom=from_jid,
                                 pstatus=STATUS,
                                 ptype="probe"
#                                 ptype="subscribe"
                                 )

        if outmsg:
          logging.info ("rec:%s roster:%s" , rec, self.xmpp.roster[from_jid][rec].resources)
          nodes = sorted([(v.get('priority',0),k) 
                          for k,v in self.xmpp.roster[from_jid][rec].resources.items()])

          nodes = nodes[-2:]
          rec_list = []
          if nodes:
            rec_list = ['/'.join([rec, n[1]]) for n in nodes]
            logging.info('actually sending to %s', rec_list)
          else:
            rec_lits = [rec]

          for r in rec_list:
            self.xmpp.sendMessage(r, outmsg, mfrom=from_jid, mtype='chat')

    else:
      to_str = str(event['to'])
      msg_str = str(event['body'])
      from_str = str(event['from'])
      payload = dict(state='old',
                     to_str=to_str,
                     from_str=from_str,
                     message_str=msg_str)

#      self.xmpp.sendPresenceSubscription(pfrom=MY_CONTROL,
#                                         ptype='subscribe',
#                                         pto=PARTYCHAPP_CONTROL)
#      self.xmpp.sendPresence(pto=PARTYCHAPP_CONTROL, pfrom=MY_CONTROL,
#                             pstatus="presence probe",
#                               ptype="subscribed")
#      self.xmpp.sendPresence(pto=PARTYCHAPP_CONTROL, pfrom=MY_CONTROL,
#                             pstatus="presence probe")

#      if not self.xmpp.roster[MY_CONTROL][PARTYCHAPP_CONTROL].resources:
#        self.xmpp.sendPresence(pto=PARTYCHAPP_CONTROL, pfrom=MY_CONTROL,
#                               ptype='subscribe')

      self.xmpp.sendMessage(PARTYCHAPP_CONTROL,
                            json.dumps(payload),
                            mfrom=MY_CONTROL,
                            mtype='chat')
                            
      logging.info('Got message <%s> to <%s> from <%s>',
                   msg_str, to_str, from_str)

    return 

  def start(self) :
    self.xmpp.connect()
    self.xmpp.process()
