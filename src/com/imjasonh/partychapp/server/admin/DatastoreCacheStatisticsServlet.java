package com.imjasonh.partychapp.server.admin;

import com.imjasonh.partychapp.MemcacheCachingDatastore;

import net.sf.jsr107cache.CacheStatistics;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Dumps statistics about the memcache cache that backs {@link 
 * MemcacheCachingDatastore}.
 *
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class DatastoreCacheStatisticsServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    resp.setContentType("text/plain");
    Writer writer = resp.getWriter();
    
    CacheStatistics stats = MemcacheCachingDatastore.getCacheStatistics();
    if (stats == null) {
      writer.write("No statistics found.\n");
      return;
    }
    
    writer.write("Cache hits: " + stats.getCacheHits() + "\n");
    writer.write("Cache misses: " + stats.getCacheMisses() + "\n");
    writer.write("Object count: " + stats.getObjectCount() + "\n");
  }
}
