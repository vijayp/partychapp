package com.imjasonh.partychapp;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class WebRequest {
  private final Map<String, List<String>> params;
  
  public WebRequest(Map<String, String[]> params) {
    this.params = Maps.newHashMap();
    for (Map.Entry<String, String[]> e : params.entrySet()) {
      this.params.put(e.getKey(), Lists.newArrayList(e.getValue()));
    }
  }

  @SuppressWarnings({"cast", "unchecked"})
  public WebRequest(HttpServletRequest req) {
    this((Map<String, String[]>) req.getParameterMap());
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
