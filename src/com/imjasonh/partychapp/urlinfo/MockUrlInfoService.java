package com.imjasonh.partychapp.urlinfo;

import java.net.URI;
import java.util.Map;

public class MockUrlInfoService implements UrlInfoService {
  private final Map<URI, UrlInfo> responses;
  
  public MockUrlInfoService(Map<URI, UrlInfo> responses) {
    this.responses = responses;
  }

  @Override public UrlInfo getUrlInfo(URI url) {
    if (!responses.containsKey(url)) {
      throw new RuntimeException("Unexpected URL " + url);
    }
    return responses.get(url);
  }

}
