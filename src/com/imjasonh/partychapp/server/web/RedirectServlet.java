package com.imjasonh.partychapp.server.web;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Simple servlet that sends a redirect to another URL (specified via a "target"
 * parameter).
 * 
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class RedirectServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws IOException {
      resp.sendRedirect(getServletConfig().getInitParameter("target"));
    }
}
