package com.imjasonh.partychapp.mr;

import java.util.Collections;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.CounterGroup;
import org.apache.hadoop.mapreduce.Counters;
import org.apache.hadoop.mapreduce.JobID;

import com.google.appengine.api.datastore.AsyncDatastoreService;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.repackaged.com.google.common.base.Joiner;
import com.google.appengine.tools.mapreduce.MapReduceState;

public class UCMRDoneServlet extends HttpServlet {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  public void doPost(HttpServletRequest req, HttpServletResponse resp) {
    String jobIdName = req.getParameter("job_id");
    JobID jobId = JobID.forName(jobIdName);
    AsyncDatastoreService asyncDS = DatastoreServiceFactory.getAsyncDatastoreService();
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    MapReduceState mrState;
    // TODO: wipe the table
    try {
      mrState = MapReduceState.getMapReduceStateFromJobID(
          datastore, jobId);
      Counters counters = mrState.getCounters();
      CounterGroup chan_data= counters.getGroup("channel");
      CounterGroup userData = counters.getGroup("user");
      SortedMap<Long, String> UserCountMap = new TreeMap<Long,String>(Collections.reverseOrder());
      SortedMap<Long, String> ChannelCountMap = new TreeMap<Long,String>(Collections.reverseOrder());
      
      for (Iterator<Counter> i = chan_data.iterator(); i.hasNext();) {
        final Counter c = i.next();
        ChannelCountMap.put(c.getValue(), c.getName());
      }
      for (Iterator<Counter> i = userData.iterator(); i.hasNext();) {
        final Counter c = i.next();
        UserCountMap.put(c.getValue(), c.getName());
      }
      String channel = Joiner.on("\n").withKeyValueSeparator(", ").join(ChannelCountMap);
      String user = Joiner.on("\n").withKeyValueSeparator(", ").join(UserCountMap);
      
      
      Entity channel_data = new Entity("processed_stats");
      channel_data.setProperty("csv",
          new com.google.appengine.api.datastore.Text(channel.substring(0, Math.min(channel.length(), 100000)))); 
      channel_data.setProperty("title", "channel");
      Entity user_data = new Entity("processed_stats");
      user_data.setProperty("csv",
          new com.google.appengine.api.datastore.Text(user.substring(0, Math.min(channel.length(), 100000)))); 
      user_data.setProperty("title", "user");
      asyncDS.put(user_data);
      asyncDS.put(channel_data);
    } catch (EntityNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

}
