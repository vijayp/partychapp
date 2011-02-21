package com.imjasonh.partychapp.server.web;

import com.google.appengine.api.users.User;
import com.google.common.base.Strings;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.server.command.ShareHandler;
import com.imjasonh.partychapp.urlinfo.ChainedUrlInfoService;
import com.imjasonh.partychapp.urlinfo.UrlInfo;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet for sharing a URL with a channel (the GET version displays the share
 * form, the POST version sends the shared URL to the channel).
 *
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class ChannelShareServlet extends AbstractChannelUserServlet {
  public static class ShareData {
    private final URI url;
    private final String annotation;
    private final String title;
    private final String description;
    
    private ShareData(
        URI url, String annotation, String title, String description) {
      this.url = url;
      this.annotation = annotation;
      this.title = title;
      this.description = description;
    }
    
    public URI getUrl() {
      return url;
    }
    
    public String getAnnotation() {
      return annotation;
    }
    
    public String getTitle() {
      return title;
    }
    
    public String getDescription() {
      return description;
    }
    
    public static ShareData fromRequest(HttpServletRequest req) {
      if (Strings.isNullOrEmpty(req.getParameter("url"))) {
        return null;
      }
      
      URI url;
      try {
        url = new URI(req.getParameter("url"));
      } catch (URISyntaxException err) {
        return null;
      }

      String annotation = req.getParameter("annotation");
      if (annotation == null) {
        annotation = "";
      }

      String title = req.getParameter("title");
      if (title == null) {
        title = "";
      }
      
      String description = req.getParameter("description");
      if (description == null) {
        description = "";
      }
      
      if (title.isEmpty() && description.isEmpty()) {
        UrlInfo urlInfo = ChainedUrlInfoService.DEFAULT_SERVICE.getUrlInfo(url);
        title = urlInfo.getTitle();
        description = urlInfo.getDescription();
      }

      return new ShareData(url, annotation, title, description);
    }
  }
  
  @Override protected void doChannelGet(
      HttpServletRequest req,
      HttpServletResponse resp,
      User user,
      Channel channel)
      throws IOException, ServletException {
    ShareData shareData = ShareData.fromRequest(req);
    if (shareData == null) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    
    RequestDispatcher disp =
      getServletContext().getRequestDispatcher(
          "/channel-share.jsp");
    req.setAttribute("channel", channel);
    req.setAttribute("shareData", shareData);
    disp.forward(req, resp);
  }
  
  @Override protected void doChannelPost(
      HttpServletRequest req,
      HttpServletResponse resp,
      User user,
      Channel channel)
      throws IOException {
    ShareData shareData = ShareData.fromRequest(req);
    if (shareData == null) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    
    Member member = channel.getMemberByJID(user.getEmail());    
    
    ShareHandler.sendShareBroadcast(
        channel,
        member,
        shareData.getUrl(),
        shareData.getAnnotation(),
        shareData.getTitle(),
        shareData.getDescription());
    
    resp.sendRedirect(channel.webUrl());
  }

}
