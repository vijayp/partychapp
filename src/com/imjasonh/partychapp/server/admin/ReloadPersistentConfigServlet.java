package com.imjasonh.partychapp.server.admin;

import com.imjasonh.partychapp.Configuration;
import com.imjasonh.partychapp.PersistentConfiguration;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Reloads the {@link PersistentConfiguration} that is cached by
 * {@link Configuration}.
 * 
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class ReloadPersistentConfigServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    resp.setContentType("text/html");
    Writer writer = resp.getWriter();
    
    Configuration.reloadPersistentConfig();
    
    resp.getWriter().write("Reloaded");
  }
}
