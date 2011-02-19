package com.imjasonh.partychapp.urlinfo;

import com.google.appengine.api.urlfetch.FetchOptions;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.ResponseTooLargeException;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;

import org.apache.commons.lang.StringEscapeUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Basic implementation of {@link UrlInfoService} that fetches URL contents with
 * App Engine's urlfetch service and then parses the result with regular
 * expressions to extract the page title.
 *
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class SimpleUrlInfoService implements UrlInfoService {
  private static final Pattern TITLE_START = Pattern.compile("<title[^>]*>");
  private static final String TITLE_END = "</title>";

  private static final Logger logger = Logger.getLogger(SimpleUrlInfoService.class.getName());
  
  @Override
  public UrlInfo getUrlInfo(URI url) {
    String content = getUriContents(url);
    if (Strings.isNullOrEmpty(content)) {
      return UrlInfo.EMPTY;
    }
    String contentLower = content.toLowerCase();
    
    Matcher titleStartMatcher = TITLE_START.matcher(contentLower);
    if (titleStartMatcher.find()) {
      int titleEnd = contentLower.indexOf(TITLE_END, titleStartMatcher.end());
      if (titleEnd != -1) {
        String title =
            content.substring(titleStartMatcher.end(), titleEnd);
        title = StringEscapeUtils.unescapeHtml(title);
        // Normalize newlines and other whitespace
        title = title.replaceAll("\\s+", " ").trim();
        return new UrlInfo(title, "");
      }
    }
    
    return UrlInfo.EMPTY;
  }
  
  @VisibleForTesting protected String getUriContents(URI uri) {
    try {
      URLFetchService urlFetchService = 
          URLFetchServiceFactory.getURLFetchService();
      FetchOptions fetchOptions = FetchOptions.Builder
          .allowTruncate()
          .followRedirects()
          .setDeadline(5.0);
      HTTPRequest request =
          new HTTPRequest(uri.toURL(), HTTPMethod.GET, fetchOptions);
      HTTPResponse response = urlFetchService.fetch(request);
      return new String(response.getContent(), Charset.forName("UTF-8"));
    } catch (MalformedURLException err) {
      // Ignore, but all URIs should be URLs
      logger.warning("Malformed URL: " + uri);
    } catch (IOException err) {
      // Ignore, don't care if we can't fetch the URL
    } catch (ResponseTooLargeException err) {
      // Shouldn't happen, since we allow truncation, but just in case.
    }
    
    // Fallthrough
    return null;
  }  

}
