package com.imjasonh.partychapp;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.google.appengine.repackaged.com.google.common.collect.Lists;
import com.google.appengine.repackaged.com.google.common.collect.Maps;

public class WebRequest {
  private String path;
  private Map<String, List<String>> params;
  
  public WebRequest(String path, Map<String, String[]> params) {
    this.path = path;
    this.params = Maps.newHashMap();
    for (Map.Entry<String, String[]> e : params.entrySet()) {
      this.params.put(e.getKey(), Lists.newArrayList(e.getValue()));
    }
  }

  @SuppressWarnings("unchecked")
  public WebRequest(HttpServletRequest req) {
    this(req.getRequestURI(), (Map<String, String[]>)req.getParameterMap());
  }
  
  public List<String> getParameterValues(String arg) {
    return params.get(arg);
  }

  public String getParameter(String arg) {
    List<String> values = params.get(arg);
    if (values != null) {
      return values.get(0);
    }
    return null;
  }
}
