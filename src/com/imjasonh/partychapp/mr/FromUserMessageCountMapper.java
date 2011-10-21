package com.imjasonh.partychapp.mr;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Logger;


import org.apache.hadoop.io.NullWritable;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.mapreduce.AppEngineMapper;
public class FromUserMessageCountMapper extends AppEngineMapper<Key, Entity, NullWritable, NullWritable> {
  private static final Logger log = Logger.getLogger(FromUserMessageCountMapper.class.getName());

  public FromUserMessageCountMapper() {
    // TODO Auto-generated constructor stub
  }

  @Override
  public void taskSetup(Context context) {
    ;
  }

  @Override
  public void taskCleanup(Context context) {
    ;
  }

  @Override
  public void setup(Context context) {
    ;
  }

  @Override
  public void cleanup(Context context) {
  }

  @Override
  public void map(Key key, Entity value, Context context) {
    final String from = (String)value.getProperty("from");
    final String to = (String)value.getProperty("to");
    final long num_r = ((Long)value.getProperty("num_recipients")).longValue();
    final long payload = ((Long)value.getProperty("payload_size")).longValue();
    final long time_ms = ((Long)value.getProperty("time_ms")).longValue();
    
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(time_ms);
    DateFormat formatter = new SimpleDateFormat("yyyyMMdd");

    String [] prefixes = { "total-",
        //formatter.format(calendar.getTime()) + "-"
        };
    
    for (String prefix : prefixes) {
      context.getCounter(prefix + "fanout-messages-channel", to).increment(num_r);
      context.getCounter(prefix + "fanout-messages-user", from).increment(num_r);
      context.getCounter(prefix + "fanout-messages-user-channel", from + " :: " + to).increment(num_r);

      context.getCounter(prefix + "messages-channel", to).increment(1);
      context.getCounter(prefix + "messages-user", from).increment(1);
      context.getCounter(prefix + "messages-user-channel", from + " :: " + to).increment(1);


      context.getCounter(prefix + "fanout-bytes-channel", to).increment(num_r * payload);
      context.getCounter(prefix + "fanout-bytes-user", from).increment(num_r * payload);
      context.getCounter(prefix + "fanout-bytes-user-channel", from + " :: " + to).increment(num_r * payload);
    }    
  }
}

