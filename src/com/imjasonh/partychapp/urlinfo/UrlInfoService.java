package com.imjasonh.partychapp.urlinfo;

import java.net.URI;

/**
 * Service that allows metadata about a URL to be extracted.
 *
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public interface UrlInfoService {
  /**
   * Attempts to get metadata about a URL. Should not throw an exception, URLs
   * whose metadata cannot be looked up should return {@link UrlInfo#EMPTY}. 
   */
  UrlInfo getUrlInfo(URI url);
}
