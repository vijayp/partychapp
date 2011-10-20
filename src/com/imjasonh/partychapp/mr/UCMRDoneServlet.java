package com.imjasonh.partychapp.mr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.CounterGroup;
import org.apache.hadoop.mapreduce.Counters;
import org.apache.hadoop.mapreduce.JobID;
import com.google.appengine.api.datastore.Text;

import com.google.appengine.api.datastore.AsyncDatastoreService;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.repackaged.com.google.common.base.Joiner;
import com.google.appengine.tools.mapreduce.MapReduceState;

import de.toolforge.googlechartwrapper.Dimension;
import de.toolforge.googlechartwrapper.PieChart;
import de.toolforge.googlechartwrapper.data.PieChartSlice;
import de.toolforge.googlechartwrapper.label.ChartLegend;

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
          new Text(txt.substring(0, Math.min(txt.length(), 1<<19)))); 
        // This really shouldn't be done here ...
        
        PieChart pieChart = new PieChart(new Dimension(700, 399));
        ArrayList<String> legend = new ArrayList<String>();
        for(Map.Entry<Long,String> entry : countMap.entrySet()) {
          pieChart.addPieChartSlice(
              new PieChartSlice.PieChartSliceBuilder(entry.getKey().intValue())/*.label(entry.getValue())*/.build());
          legend.add(entry.getValue());
          if (legend.size() > 100) {
            break;
          }
        }
        pieChart.setChartLegend(new ChartLegend(legend));
        summary_entity.setProperty("image_url1", new Text(pieChart.getUrl()));
        ///
        
        asyncDS.put(summary_entity);
      }
    } catch (EntityNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
