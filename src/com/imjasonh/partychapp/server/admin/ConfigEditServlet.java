package com.imjasonh.partychapp.server.admin;

import com.google.common.base.Strings;

import com.imjasonh.partychapp.Configuration;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.PersistentConfiguration;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Displays a form UI for editing the {@link PersistentConfiguration} instance
 * that is used by {@link Configuration}.
 * 
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class ConfigEditServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    RequestDispatcher disp =
        getServletContext().getRequestDispatcher("/admin/config-edit.jsp");
    disp.forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    PersistentConfiguration config = Configuration.persistentConfig();

    config.setSessionToken(getParam(req, "session-token"));
    config.setListFeedUrl(getParam(req, "list-feed-url"));
    config.setChannelStatsEnabled(
        Boolean.parseBoolean(req.getParameter("channel-stats-enabled")));
    config.setEmbedlyKey(getParam(req, "embedly-key"));
    config.setFractionOfMessagesToLog(Double.parseDouble(getParam(req, "fraction-log")));

    Datastore datastore = Datastore.instance();

    datastore.startRequest();
    datastore.put(config);
    datastore.endRequest();    

    resp.sendRedirect("/admin/config");
  }

  /**
   * Converts the literal "null" string back into a null.
   */
  private static String getParam(HttpServletRequest req, String name) {
    String value = req.getParameter(name);
    if (Strings.isNullOrEmpty(value) || value.equals("null")) {
      return null;
    }
    return value;
  }
}
