package com.imjasonh.partychapp.server.admin;

import com.imjasonh.partychapp.ChannelStats;
import com.imjasonh.partychapp.ChannelStats.ChannelStat;

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
  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    resp.setContentType("text/html");
    Writer writer = resp.getWriter();

    ChannelStats stats = ChannelStats.getCurrentStats();
    
    if (stats == null) {
      writer.write("No stats found");
      return;
    }

    writer.write("Since: " + stats.getCreationDate() + "<br>");
    writer.write("As of: " + stats.getLastUpdateDate() + "<br>");
    writer.write("Total byte count: " + 
        NUMBER_FORMAT.format(stats.getTotalByteCount()) + "<br>");
    
    writer.write("<table>");
    writer.write("<tr>");
    writer.write("<th>Channel Name</th>");
    writer.write("<th>Outgoing byte count</th>");
    writer.write("<th>Member count</th>");
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
      
      writer.write("</tr>");
    }
    
    writer.write("</table>");
  }
}
