package com.imjasonh.partychapp.server;

import com.google.appengine.api.xmpp.Message;
import com.google.appengine.api.xmpp.XMPPService;
import com.google.appengine.api.xmpp.XMPPServiceFactory;
import com.google.common.base.Joiner;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet for handling XMPP error notifications from the App Engine XMPP
 * service.
 *
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class PartychappErrorServlet extends HttpServlet {
  private static final Logger logger =
      Logger.getLogger(PartychappServlet.class.getName());

  private static final Joiner JOINER = Joiner.on(", ");

  private static final XMPPService XMPP = XMPPServiceFactory.getXMPPService();

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    logger.warning("Error servlet request");

    try {
      Message xmppMessage = XMPP.parseMessage(req);
      logger.warning("message type: " + xmppMessage.getMessageType());
      logger.warning("from: " + xmppMessage.getFromJid());
      logger.warning("recipients: " + JOINER.join(xmppMessage.getRecipientJids()));
      logger.warning("body: " + xmppMessage.getBody());
      logger.warning("stanza: " + xmppMessage.getStanza());
      logger.warning("isXml: " + xmppMessage.isXml());
    } catch (IllegalArgumentException e) {
      // These exceptions are apparently caused by a bug in the gtalk flash
      // gadget, so let's just ignore them.
      // http://code.google.com/p/googleappengine/issues/detail?id=2082
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    resp.setStatus(HttpServletResponse.SC_OK);
  }
}
