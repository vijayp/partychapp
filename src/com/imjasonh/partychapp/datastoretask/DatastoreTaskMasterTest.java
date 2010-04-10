package com.imjasonh.partychapp.datastoretask;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import com.google.appengine.api.xmpp.JID;
import com.google.appengine.repackaged.com.google.common.collect.Maps;
import com.google.appengine.repackaged.com.google.common.collect.Sets;
import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Configuration;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.FakeDatastore;
import com.imjasonh.partychapp.WebRequest;

public class DatastoreTaskMasterTest extends TestCase {
  FakeDatastore fd;
  DatastoreTaskMaster dtm = new DatastoreTaskMaster();
  FakeQueue tq = new FakeQueue();
  
  @Override
  public void setUp() {
    fd = new FakeDatastore();
    Datastore.setInstance(fd);
  }

  public void addNChannels(int n) {
    String[] numbers = new String[]{ "00", "01", "02", "03", "04", "05", "06", "07", "08", "09",
            "10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
    };
    for (int i = 0; i < n; ++i) {
      String name = "pancake" + numbers[i];
      Channel c = new Channel(new JID(name + "@" + Configuration.chatDomain));
      c.addMember(new JID("neil@gmail.com"));
      c.put();
    }
  }
  
  public void runTaskMaster() {
    Map<String, String[]> params = Maps.newHashMap();
    params.put("act", new String[]{ DatastoreTaskMaster.Action.FIX_CHANNELS.name() });
    dtm.handle(new WebRequest(params), tq);    
  }
  
  public void assertURIsEquiv(String expected, TestableQueue.Options actual) {
    URI expURI = URI.create(expected);
    assertEquals(expURI.getPath(), actual.url());
    Set<String> expParams = Sets.newHashSet(expURI.getQuery().split("&"));
    Set<String> actParams = Sets.newHashSet(actual.params());
    // union - intersection = everything in one but not another
    Set<String> diff = Sets.difference(Sets.union(expParams, actParams),
                                       Sets.intersection(expParams, actParams));
    assertEquals("Expected: " + expParams + ", Actual: " + actParams + ", Diff: " + diff,
                 0, diff.size());
    assertEquals("Expected: " + expParams + ", Actual: " + actParams,
                 expParams.size(), actParams.size());
  }
  
  public void testMultipleSubtasks() {
    addNChannels(11);
    runTaskMaster();
    assertEquals(3, tq.getTasks().size());
    assertURIsEquiv("/tasks/FIX_CHANNELS?key=pancake00&key=pancake01&key=pancake02&key=pancake03&key=pancake04&key=pancake05&key=pancake06&key=pancake07&key=pancake08&key=pancake09",
                    tq.getTasks().get(0));
    assertURIsEquiv("/tasks/FIX_CHANNELS?key=pancake10", tq.getTasks().get(1));
    assertURIsEquiv("/tasks/MASTER_TASK?act=FIX_CHANNELS&lastKeyHandled=pancake10", tq.getTasks().get(2));
  }
  
  public void testResume() {
    addNChannels(12);
    Map<String, String[]> params = Maps.newHashMap();
    params.put("act", new String[]{ DatastoreTaskMaster.Action.FIX_CHANNELS.name() });
    params.put("lastKeyHandled", new String[]{ "pancake09" });
    dtm.handle(new WebRequest(params), tq);    

    assertEquals(2, tq.getTasks().size());
    assertURIsEquiv("/tasks/FIX_CHANNELS?key=pancake10&key=pancake11",
                    tq.getTasks().get(0));
    assertURIsEquiv("/tasks/MASTER_TASK?act=FIX_CHANNELS&lastKeyHandled=pancake11", tq.getTasks().get(1));
  }
  
  public void testOneCompleteSubtask() {
    addNChannels(10);
    runTaskMaster();
    assertEquals(2, tq.getTasks().size());
    assertURIsEquiv("/tasks/FIX_CHANNELS?key=pancake00&key=pancake01&key=pancake02&key=pancake03&key=pancake04&key=pancake05&key=pancake06&key=pancake07&key=pancake08&key=pancake09",
                    tq.getTasks().get(0));
    assertURIsEquiv("/tasks/MASTER_TASK?act=FIX_CHANNELS&lastKeyHandled=pancake09", tq.getTasks().get(1));
  }
  
  public void testOnePartialSubtask() {
    addNChannels(7);
    runTaskMaster();
    assertEquals(2, tq.getTasks().size());
    assertURIsEquiv("/tasks/FIX_CHANNELS?key=pancake00&key=pancake01&key=pancake02&key=pancake03&key=pancake04&key=pancake05&key=pancake06",
                    tq.getTasks().get(0));
    assertURIsEquiv("/tasks/MASTER_TASK?act=FIX_CHANNELS&lastKeyHandled=pancake06", tq.getTasks().get(1));
  }

  public void testResumeAtEnd() {
    addNChannels(12);
    Map<String, String[]> params = Maps.newHashMap();
    params.put("act", new String[]{ DatastoreTaskMaster.Action.FIX_CHANNELS.name() });
    params.put("lastKeyHandled", new String[]{ "pancake11" });
    dtm.handle(new WebRequest(params), tq);    

    assertEquals(0, tq.getTasks().size());
  }

  
  public void testNoInput() {
    runTaskMaster();
    assertEquals(0, tq.getTasks().size());
  }
  
  public void testWithMaxElements() {
    addNChannels(12);
    Map<String, String[]> params = Maps.newHashMap();
    params.put("act", new String[]{ DatastoreTaskMaster.Action.FIX_CHANNELS.name() });
    params.put("max", new String[]{ "11" });
    dtm.handle(new WebRequest(params), tq);    

    assertEquals(2, tq.getTasks().size());
    assertURIsEquiv("/tasks/FIX_CHANNELS?key=pancake00&key=pancake01&key=pancake02&key=pancake03&key=pancake04&key=pancake05&key=pancake06&key=pancake07&key=pancake08&key=pancake09",
                    tq.getTasks().get(0));
    assertURIsEquiv("/tasks/FIX_CHANNELS?key=pancake10",
                    tq.getTasks().get(1));
  }
}
