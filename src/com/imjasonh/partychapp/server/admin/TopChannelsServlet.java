package com.imjasonh.partychapp.server.admin;

import com.google.appengine.api.quota.QuotaService;
import com.google.appengine.api.quota.QuotaServiceFactory;

import com.imjasonh.partychapp.Configuration;
import com.imjasonh.partychapp.stats.ChannelStats;
import com.imjasonh.partychapp.stats.ChannelStats.ChannelStat;

import java.io.IOException;
import java.io.Writer;
import java.text.NumberFormat;
import java.util.Locale;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Dumps top channels by bytes sent.
 * 
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class TopChannelsServlet extends HttpServlet {
  private static final NumberFormat NUMBER_FORMAT =
      NumberFormat.getIntegerInstance(Locale.US);

  private static final NumberFormat CPU_FORMAT =
    NumberFormat.getNumberInstance(Locale.US);
  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    resp.setContentType("text/html");
    Writer writer = resp.getWriter();

    if (!Configuration.persistentConfig().areChannelStatsEnabled()) {
      writer.write("Channel stats are not enabled");
      return;
    }
    QuotaService qs = QuotaServiceFactory.getQuotaService();
    
    
    if ("true".equals(req.getParameter("reset"))) {
      ChannelStats.reset();
    }

    ChannelStats stats = ChannelStats.getCurrentStats();
    
    if (stats == null) {
      writer.write("No stats found");
      return;
    }

    writer.write("Since: " + stats.getCreationDate() + "<br>");
    writer.write("As of: " + stats.getLastUpdateDate() + "<br>");
    writer.write("Total byte count: " + 
        NUMBER_FORMAT.format(stats.getTotalByteCount()) + "<br>");
    writer.write("Total message count (pre-fanout): " + 
        NUMBER_FORMAT.format(stats.getTotalMessagePreFanoutCount()) + "<br>");
    writer.write("Total message count (post-fanout): " + 
        NUMBER_FORMAT.format(stats.getTotalMessagePostFanoutCount()) + "<br>");
    writer.write("Total CPU seconds used: " +
        CPU_FORMAT.format(
            qs.convertMegacyclesToCpuSeconds(stats.getTotalCpuMegaCycles())));

    writer.write("<table>");
    writer.write("<tr>");
    writer.write("<th>Channel Name</th>");
    writer.write("<th>Outgoing byte count</th>");
    writer.write("<th>Member count</th>");
    writer.write("<th>Message count<br>(pre-fanout)</th>");
    writer.write("<th>Message count<br>(post-fanout)</th>");
    writer.write("<th>CPU seconds</th>");
    writer.write("</tr>");
    
    for (ChannelStat stat : stats.getTopChannels()) {
      String htmlChannelName = stat.getChannelName()
          .replaceAll("&", "&amp;")
          .replaceAll("<", "&lt;")
          .replaceAll(">", "&gt;");
      writer.write("<tr>");
      
      writer.write("<td>");
      writer.write("<a href=\"/admin/channel/" + 
          htmlChannelName + "\">" + htmlChannelName + "</a>");
      writer.write("</td>");
      
      writer.write("<td style=\"text-align: right\">");
      writer.write(NUMBER_FORMAT.format(stat.getByteCount()));
      writer.write("</td>");
      
      writer.write("<td style=\"text-align: right\">");
      writer.write(NUMBER_FORMAT.format(stat.getMemberCount()));
      writer.write("</td>");
      
      writer.write("<td style=\"text-align: right\">");
      writer.write(NUMBER_FORMAT.format(stat.getMessagePreFanoutCount()));
      writer.write("</td>");
            
      writer.write("<td style=\"text-align: right\">");
      writer.write(NUMBER_FORMAT.format(stat.getMessagePostFanoutCount()));
      writer.write("</td>");
      
      writer.write("<td style=\"text-align: right\">");
      writer.write(CPU_FORMAT.format(
          qs.convertMegacyclesToCpuSeconds(stat.getCpuMegaCycles())));
      writer.write("</td>");
      
      writer.write("</tr>");
    }
    
    writer.write("</table>");
  }
}
