package com.imjasonh.partychapp.mr;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
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
import com.google.appengine.api.datastore.Text;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.taskqueue.Transaction;
import com.google.appengine.repackaged.com.google.common.base.Joiner;
import com.google.appengine.tools.mapreduce.MapReduceState;
import com.google.apphosting.api.ApiProxy.RequestTooLargeException;
import com.imjasonh.partychapp.server.PartychappServlet;

import de.toolforge.googlechartwrapper.Dimension;
import de.toolforge.googlechartwrapper.PieChart;
import de.toolforge.googlechartwrapper.data.PieChartSlice;
import de.toolforge.googlechartwrapper.label.ChartLegend;

public class UCMRDoneServlet extends HttpServlet {

  /**
   * 
   */
  private static final Logger logger =
      Logger.getLogger(PartychappServlet.class.getName());

  private static final long serialVersionUID = 1L;
  public void doPost(HttpServletRequest req, HttpServletResponse resp) {
    
    String jobIdName = req.getParameter("job_id");
    JobID jobId = JobID.forName(jobIdName);
    MapReduceState mrState;
    // TODO: wipe the table
    try {
      {
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      mrState = MapReduceState.getMapReduceStateFromJobID(
          datastore, jobId);
      }
      Counters counters = mrState.getCounters();
      for (Iterator<CounterGroup> cg_it = counters.iterator(); cg_it.hasNext();) {
        CounterGroup cg = cg_it.next();
        SortedMap<Long, String> countMap = new TreeMap<Long,String>(Collections.reverseOrder());
        for (Iterator<Counter> i = cg.iterator(); i.hasNext();) {
          final Counter c = i.next();
          countMap.put(c.getValue(), c.getName());
        }
        String txt = Joiner.on("\n").withKeyValueSeparator(", ").join(countMap);
        Entity summary_entity = new Entity("stats_table", cg.getName());
        summary_entity.setProperty("title", cg.getName());
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
        {
          logger.info("Trying to log entity of size " + summary_entity.toString().length());
          DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
          com.google.appengine.api.datastore.Transaction t = datastore.beginTransaction();
          datastore.put(summary_entity);
          t.commit();
        }
      }
    } catch (EntityNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (RequestTooLargeException e) {
      e.printStackTrace();
    }
  }
}
