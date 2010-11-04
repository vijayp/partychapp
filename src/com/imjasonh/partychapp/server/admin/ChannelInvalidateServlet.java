package com.imjasonh.partychapp.server.admin;

import com.google.common.base.Strings;

import com.imjasonh.partychapp.CachingDatastore;
import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Invalidates cached {@link Channel} data. 
 *
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class ChannelInvalidateServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    resp.setContentType("text/html");
    Writer writer = resp.getWriter();
    
    if (Strings.isNullOrEmpty(req.getPathInfo())) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    // Strip leading slash to get channel name
    String channelName = req.getPathInfo().substring(1);
    
    Datastore datastore = Datastore.instance();
    
    if (!(datastore instanceof CachingDatastore)) {
      writer.write("Not using a CachingDastore");
      return;
    }
    
    CachingDatastore cachingDatastore = (CachingDatastore) datastore;
    cachingDatastore.startRequest();
    Channel channel = cachingDatastore.getChannelByName(channelName);
    cachingDatastore.invalidateCacheIfNecessary(channel);
    cachingDatastore.endRequest();
    
    writer.write("Reloaded.<br>");
    writer.write("<a href=\"/admin/channel/" + channelName + "\">View channel</a>");
  }
}
