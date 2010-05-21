package com.imjasonh.partychapp.server;

import com.google.common.base.Strings;

import javax.servlet.http.HttpServletRequest;

/**
 * HTTP/servlet-related utility code.
 * 
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class HttpUtil {
  /**
   * Gets the original URL that was requested.
   */
  public static String getRequestUri(HttpServletRequest request) {
    // In the case of request forwarding, the original URL is stored as an
    // attribute (see section 8.4.2 of the Java Servlet Specification 2.4)
    String originalUri =
        (String) request.getAttribute("javax.servlet.forward.request_uri");

    if (!Strings.isNullOrEmpty(originalUri)) {
      return originalUri;
    }
    
    return request.getRequestURI();
  }
  
  private HttpUtil() {
    // Not instantiable
  }
}
