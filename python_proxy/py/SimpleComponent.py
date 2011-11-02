import sys
sys.path.append("../3rdParty")
import sleekxmpp.componentxmpp
import logging
import simplejson as json

MYDOMAIN = 'im.partych.at'

PARTYCHAPP_CONTROL = '__control@partychapp.appspot.com'
#PARTYCHAPP_CONTROL = 'newpc_test@partychapp.appspotchat.com'
MY_CONTROL = '_control@im.partych.at'
class SimpleComponent:
  @staticmethod
  def GetControlMessage(event):
    
    if str(event['to']) != MY_CONTROL:
      logging.info('to is <%s> but not <_control@im.partych.at>', type(event['to']))
      return None
    else:
      # TODO: check from, and check signature of message
      msg_str = str(event['body'])
      logging.info('decoding <%s>', msg_str)
      return json.loads(msg_str)

    
  def __init__(self, jid, password, server, port, backend) :
    
    self.xmpp = sleekxmpp.componentxmpp.ComponentXMPP(jid, password, server, port)
    self.xmpp.auto_authorize = True
    self.xmpp.auto_subscribe = True
    # for event in ["message", "got_online", "got_offline", "changed_status"] :
    for event in ["message"]:
      self.xmpp.add_event_handler(event, self.handleIncomingXMPPEvent)
#    for event in ["got_online", "got_offline", "changed_status"] :
#      self.xmpp.add_event_handler(event, self.handle_probe)
    self._jid_resource_map = {}

  def handle_probe(self, presence):
    sender = presence['from']
    if presence['type'] == 'unavailable':
      logging.info('%s has gone offline', sender)
    else:
      logging.info('%s has come online', sender)

    # Populate the presence reply with the agent's current status.
    #self.sendPresence(pto=sender, pstatus="Busy studying XMPP", pshow="dnd")


  def handleIncomingXMPPEvent(self, event) :
    logging.debug('got xmpp incoming event for %s', event)
    logging.info('from: %s', event['from'])
    logging.info('to: %s', event['to'])
#    msgLocations = {sleekxmpp.stanza.presence.Presence: "status",
#                    sleekxmpp.stanza.message.Message: "body"}
#    assert type(event) == 
    message = event['body']
    logging.info('message: %s' % message)

    
    ctl = self.GetControlMessage(event)
    if ctl:
      outmsg = ctl.get('message')
      recipients = ctl.get('recipients', [])
      from_channel = ctl.get('channel', '')
      assert from_channel and recipients and outmsg
      assert '@' not in from_channel # TODO better validation
      from_jid = '%s@%s' % (from_channel, MYDOMAIN)

      debug_message = 'sending message <%s> to recipients[%s] from %s' % (
        outmsg, 
        ','.join(recipients),
        from_jid)
      logging.info(debug_message)
      #      self.xmpp.sendMessage(event['from'], debug_message, mfrom=from_jid)
      for rec in recipients:
        self.xmpp.sendPresenceSubscription(pfrom=from_jid,
                                           ptype='subscribe',
                                           pto=rec)
        self.xmpp.sendMessage(rec, outmsg, mfrom=from_jid, mtype='chat'
                              )
        logging.info('roster: says %s ', self.xmpp.roster[from_jid][rec].resources)

    else:
      to_str = str(event['to'])
      msg_str = str(event['body'])
      from_str = str(event['from'])
      payload = dict(to_str=to_str,
                     from_str=from_str,
                     message_str=msg_str)

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
