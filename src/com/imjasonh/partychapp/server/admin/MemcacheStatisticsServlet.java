package com.imjasonh.partychapp.server.admin;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.memcache.Stats;

import com.imjasonh.partychapp.MemcacheCachingDatastore;

import java.io.IOException;
import java.io.Writer;
import java.text.NumberFormat;
import java.util.Locale;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Dumps statistics about the memcache cache that backs {@link 
 * MemcacheCachingDatastore} and other parts of the code that uses memcache.
 *
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class MemcacheStatisticsServlet extends HttpServlet {
  private static final NumberFormat NUMBER_FORMAT =
      NumberFormat.getIntegerInstance(Locale.US);
  private static final NumberFormat PERCENT_FORMAT =
      NumberFormat.getPercentInstance(Locale.US);

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    resp.setContentType("text/plain");
    Writer writer = resp.getWriter();
    
    MemcacheService memcacheService = MemcacheServiceFactory.getMemcacheService();
    Stats stats = memcacheService.getStatistics();
    
    long hitCount = stats.getHitCount();
    long missCount = stats.getMissCount();
    long totalAccessCount = hitCount + missCount;
    double hitFraction = ((double) hitCount)/((double) totalAccessCount);
    double missFraction = ((double) missCount)/((double) totalAccessCount);
    writer.write("Hit count: " + NUMBER_FORMAT.format(stats.getHitCount()) + 
        " (" + PERCENT_FORMAT.format(hitFraction) + ")\n");
    writer.write("Miss count: " + NUMBER_FORMAT.format(stats.getMissCount()) +
        " (" + PERCENT_FORMAT.format(missFraction) + ")\n");
    writer.write(
        "Total accesses: " + NUMBER_FORMAT.format(totalAccessCount) + "\n");
    
    writer.write("Oldest entry access: " +
        NUMBER_FORMAT.format(stats.getMaxTimeWithoutAccess()) + "ms\n");
    writer.write("Bytes returned for hits: " +
        NUMBER_FORMAT.format(stats.getBytesReturnedForHits()) + "\n");
    writer.write("Total item count: " +
        NUMBER_FORMAT.format(stats.getItemCount()) + "\n");
    writer.write("Total item bytes: " +
        NUMBER_FORMAT.format(stats.getTotalItemBytes()) + "\n");
  }
}
