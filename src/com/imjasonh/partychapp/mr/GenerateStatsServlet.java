package com.imjasonh.partychapp.mr;

import java.awt.Color;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.CounterGroup;
import org.apache.hadoop.mapreduce.Counters;
import org.apache.hadoop.mapreduce.JobID;
import org.datanucleus.sco.backed.Set;

import com.google.appengine.api.datastore.Text;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.taskqueue.Transaction;
import com.google.appengine.repackaged.com.google.common.base.Joiner;
import com.google.appengine.repackaged.com.google.common.collect.Lists;
import com.google.apphosting.api.ApiProxy.RequestTooLargeException;
import com.google.common.collect.Maps;
import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.User;
import com.imjasonh.partychapp.server.PartychappServlet;

import de.toolforge.googlechartwrapper.Dimension;
import de.toolforge.googlechartwrapper.PieChart;
import de.toolforge.googlechartwrapper.data.PieChartSlice;
import de.toolforge.googlechartwrapper.label.ChartLegend;

public class GenerateStatsServlet extends HttpServlet {

  /**
   * 
   */
  private static final Logger logger =
      Logger.getLogger(PartychappServlet.class.getName());

  private static final long serialVersionUID = 1L;

  private Map<String, HashMap<String, Long> > counters = new TreeMap<String, HashMap<String, Long>>(); 

  private void increment(String group, String key, long amount) {
    if (!counters.containsKey(group)) {
      counters.put(group, new HashMap<String, Long>());
    }
    if (!counters.get(group).containsKey(key)) {
      counters.get(group).put(key, new Long(0));
    }
    counters.get(group).put(key, counters.get(group).get(key) + amount);
  }


  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query q = new Query("messageLog");
    PreparedQuery pq = datastore.prepare(q);
    HashSet<String> channelNames = new HashSet<String>();
    resp.setContentType("text/html");
    resp.getWriter().print("parsing logs");
    resp.getWriter().flush();
    int count = 0;
    for (Entity value : pq.asIterable()) {
      if ((++count % 100) == 0) {
        resp.getWriter().print(".");
        resp.getWriter().flush();
      }
      final String from = (String)value.getProperty("from");
      final String to = (String)value.getProperty("to");
      final long num_r = ((Long)value.getProperty("num_recipients")).longValue();
      final long payload = ((Long)value.getProperty("payload_size")).longValue();
      final long time_ms = ((Long)value.getProperty("time_ms")).longValue();

      Calendar calendar = Calendar.getInstance();
      calendar.setTimeInMillis(time_ms);
      DateFormat formatter = new SimpleDateFormat("yyyyMMdd");

      String [] prefixes = { "total-",
          formatter.format(calendar.getTime()) + "-"
      };

      for (String prefix : prefixes) {
        increment(prefix + "fanout-messages-channel", to, num_r);
        increment(prefix + "fanout-messages-user", from, num_r);
        increment(prefix + "fanout-messages-user-channel", from + " :: " + to, num_r);

        increment(prefix + "messages-channel", to, 1);
        increment(prefix + "messages-user", from, 1);
        increment(prefix + "messages-user-channel", from + " :: " + to, 1);


        increment(prefix + "fanout-bytes-channel", to, num_r * payload);
        increment(prefix + "fanout-bytes-user", from, num_r * payload);
        increment(prefix + "fanout-bytes-user-channel", from + " :: " + to, num_r * payload);
      }
    }
    
    if (false){ // this causes deadline exceeded errors
      SortedMap<Long, String> countMap = new TreeMap<Long,String>(Collections.reverseOrder());
      for (Map.Entry<String, Long> c : counters.get("total-fanout-bytes-channel").entrySet()) {
        countMap.put(c.getValue(), c.getKey());
      }
      int max = Math.min(6, countMap.size());
      for (Map.Entry<Long,String> i : countMap.entrySet()){
        if (max-- <= 0)
          break;
        channelNames.add(i.getValue());
      }
      Datastore ds= Datastore.instance();
      ds.startRequest();
      final int[] cutoffs = {1, -7, -15, -30, -90};
      resp.getWriter().print("<br/>parsing channels");
      resp.getWriter().flush();
      for (String channelName :channelNames){
        if (channelName == null)
          continue;
        resp.getWriter().print("<br/>" + channelName);
        resp.getWriter().flush();
        Channel channel = ds.getChannelByName(channelName);
        List<Member> members = Lists.newArrayList(channel.getMembers());

        for (Member member : members) {
          if ((++count % 100) == 0) {
            resp.getWriter().print(".");
            resp.getWriter().flush();
            logger.info(".");
          }
          User memberUser = Datastore.instance().getUserByJID(member.getJID());

          for (int cutoff : cutoffs) {
            Calendar now = Calendar.getInstance();
            now.add(Calendar.DATE, cutoff);
            Date threshold = now.getTime();
            if (memberUser != null && memberUser.lastSeen() != null && memberUser.lastSeen().before(threshold)) {
              increment("mia-user-channel" + cutoff, channelName, 1);
            }
          }
        }
      }
    }
    for (Map.Entry<String, HashMap<String, Long>> cg : counters.entrySet()) {
      SortedMap<Long, String> countMap = new TreeMap<Long,String>(Collections.reverseOrder());
      for (Map.Entry<String, Long> c : cg.getValue().entrySet()) {
        countMap.put(c.getValue(), c.getKey());
      }
      String txt = Joiner.on("\n").withKeyValueSeparator(", ").join(countMap);
      Entity summary_entity = new Entity("stats_table", cg.getKey());
      summary_entity.setProperty("title", cg.getKey());
      summary_entity.setProperty("csv",
          new Text(txt.substring(0, Math.min(txt.length(), 1<<18)))); 
      // This really shouldn't be done here ...
      PieChart pieChart = new PieChart(new Dimension(700, 399));
      ArrayList<String> legend = new ArrayList<String>();
      int leftover = 0;
      for(Map.Entry<Long,String> entry : countMap.entrySet()) {
        if (legend.size() < 15) {
          pieChart.addPieChartSlice(
              new PieChartSlice.PieChartSliceBuilder(entry.getKey().intValue())/*.label(entry.getValue())*/.build());
          legend.add(entry.getValue());
        } else {
          leftover += entry.getKey().intValue();
        }
      }
      if (leftover > 0) {
        pieChart.addPieChartSlice(
            new PieChartSlice.PieChartSliceBuilder(leftover).label("leftover").build());
        legend.add("leftover");
      }

      pieChart.setChartLegend(new ChartLegend(legend));
      summary_entity.setProperty("image_url1", new Text(pieChart.getUrl()));
      ///
      logger.info("Trying to log entity of size " + summary_entity.toString().length());
      datastore.put(summary_entity);
    }
    {
      Entity ts = new Entity("stats_table", "  Timestamp  ");
      Calendar calendar = Calendar.getInstance();
      calendar.setTimeInMillis(System.currentTimeMillis());
      DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
      ts.setProperty("title", "last generated on " + formatter.format(calendar.getTime()));
      ts.setProperty("csv", "");
      datastore.put(ts);
    }
      resp.getWriter().print("<br/> <a href=/admin/stats>Stats are here</a>");
  }

}
