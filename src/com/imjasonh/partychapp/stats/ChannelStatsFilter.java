package com.imjasonh.partychapp.stats;

import com.imjasonh.partychapp.stats.ChannelStats.ChannelStat;

import java.io.IOException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Servlet filter meant to be used in conjuction with {@link ChannelStats} to
 * allow resource costs of channels to be computed.
 * 
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class ChannelStatsFilter implements Filter {
  @Override
  public void doFilter(
      ServletRequest request,
      ServletResponse response,
      FilterChain chain)
      throws IOException, ServletException {
    ChannelStats.perRequestStats.get().clear();
    chain.doFilter(request, response);
    List<ChannelStat> requestStats = ChannelStats.perRequestStats.get();
    if (!requestStats.isEmpty()) {
      ChannelStats.recordStats(requestStats);
    }
  }

  // Unused {@link Filter} methods
  @Override public void destroy() {}
  @Override public void init(FilterConfig arg0) {}
}