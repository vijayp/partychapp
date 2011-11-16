package com.imjasonh.partychapp.server;


import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;

import com.imjasonh.partychapp.Configuration;
import com.imjasonh.partychapp.PersistentConfiguration;

@SuppressWarnings("serial")
public class ControlServlet extends HttpServlet {
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    String body = req.getParameter("body");
    String token = req.getParameter("token");
    
    if (!token.equals(Configuration.persistentConfig().getProxyToken())) {
      resp.sendError(400);
      return;
    }
    try {
      PartychappServlet.doControlPacket(body);
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      resp.sendError(500);
      return;
    }
    resp.getWriter().write("\"ok\"");
  }

  protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
    try {
      doPost(req,resp);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
