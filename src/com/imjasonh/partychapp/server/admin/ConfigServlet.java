package com.imjasonh.partychapp.server.admin;

import com.imjasonh.partychapp.PersistentConfiguration;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that displays the current {@link PersistentConfiguration} instance.
 *
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class ConfigServlet extends HttpServlet {
  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    RequestDispatcher disp =
        getServletContext().getRequestDispatcher("/admin/config.jsp");
    disp.forward(req, resp);
  }
}
