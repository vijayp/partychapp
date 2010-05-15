package com.imjasonh.partychapp.server.command;

import junit.framework.TestCase;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.server.SendUtil;
import com.imjasonh.partychapp.testing.FakeDatastore;
import com.imjasonh.partychapp.testing.MockXMPPService;

public abstract class CommandHandlerTestCase extends TestCase {
  MockXMPPService xmpp = new MockXMPPService();

  @Override
  public void setUp() {
    FakeDatastore datastore = new FakeDatastore();
    Datastore.setInstance(datastore);
    datastore.setUp();
    SendUtil.setXMPP(xmpp);
  }
}
