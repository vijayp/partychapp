package com.imjasonh.partychapp.urlinfo;

import com.google.common.collect.Lists;

import java.net.URI;
import java.util.List;

/**
 * {@link UrlInfoService} implementation which sequentially queries multiple
 * implementations and returns the first non-empty response.  
 *
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class ChainedUrlInfoService implements UrlInfoService {
  public static final UrlInfoService DEFAULT_SERVICE =
      new ChainedUrlInfoService(
          new EmbedlyUrlInfoService(), new SimpleUrlInfoService());
  
  private final List<UrlInfoService> services;
  
  public ChainedUrlInfoService(UrlInfoService... services) {
    this.services = Lists.newArrayList(services);
  }

  @Override public UrlInfo getUrlInfo(URI url) {
    for (UrlInfoService service : services) {
      UrlInfo urlInfo = service.getUrlInfo(url);
      if (!UrlInfo.EMPTY.equals(urlInfo)) {
        return urlInfo;
      }
    }

    return UrlInfo.EMPTY;
  }

}
