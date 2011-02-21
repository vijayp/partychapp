package com.imjasonh.partychapp.urlinfo;

import com.google.appengine.api.memcache.jsr107cache.GCacheFactory;
import com.google.appengine.api.urlfetch.FetchOptions;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * {@link UrlInfoService} implementation that uses Embedly's oEmbed interface
 * (http://api.embed.ly/docs/oembed) to extract metadata about a URL. Embedly
 * only handles a subset of URLs, so this service should be combined with 
 * another {@link UrlInfoService} implementation via {@link
 * ChainedUrlInfoService}.
 * 
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class EmbedlyUrlInfoService implements UrlInfoService {
  private static final Logger logger = Logger.getLogger(EmbedlyUrlInfoService.class.getName());

  private static final String SERVICE_PATTERNS_KEY = "service-patterns";
  
  private static Cache cache = null;
  static {
    try {
      cache = CacheManager.getInstance().getCacheFactory().createCache(
          ImmutableMap.of(GCacheFactory.EXPIRATION_DELTA, 24 * 60 * 60));
    } catch (CacheException err) {
      logger.warning("Could not initialize EmbedlyUrlInfoService cache");
    }
  }
  
  
  private static final URLFetchService URL_FETCH_SERVICE =
    URLFetchServiceFactory.getURLFetchService();
  private static final FetchOptions FETCH_OPTIONS = FetchOptions.Builder
      .allowTruncate()
      .followRedirects()
      .setDeadline(60.0);

  private static final String SERVICES_URI =
      "http://api.embed.ly/1/services/javascript";
  private static final String OEMBED_REQUEST_TEMPLATE =
      "http://api.embed.ly/1/oembed?url=%s";

  @Override
  public UrlInfo getUrlInfo(URI url) {
    List<Pattern> patterns = getServicePatterns();
    
    boolean matched = false;
    for (Pattern pattern : patterns) {
      if (pattern.matcher(url.toString()).matches()) {
        matched = true;
        break;
      }
    }
    
    if (!matched) {
      logger.info(url + " did not match any Embedly patterns");
      return UrlInfo.EMPTY;
    }
    
    String oembedRequestUri;
    try {
      oembedRequestUri = String.format(
            OEMBED_REQUEST_TEMPLATE,
            URLEncoder.encode(url.toString(), "UTF-8"));
    } catch (UnsupportedEncodingException err) {
      // UTF-8 is always supported
      throw new RuntimeException(err);
    }
    
    String oembedResponse = getUrlContents(oembedRequestUri);
    if (oembedResponse == null)
      return UrlInfo.EMPTY;
    
    try {
      JSONObject oembedJson = new JSONObject(oembedResponse);
      return new UrlInfo(
          oembedJson.optString("title"), oembedJson.optString("description"));
    } catch (JSONException err) {
      logger.log(Level.WARNING, "Could not parse oEmbed response", err);
    }
      
    return UrlInfo.EMPTY;
  }
  
  @SuppressWarnings("unchecked")
  private List<Pattern> getServicePatterns() {
    List<Pattern> patterns = (List<Pattern>) cache.get(SERVICE_PATTERNS_KEY);
    
    if (patterns == null) {
      logger.info("Fetching Embedly service patterns");
      patterns = fetchServicePatterns();
      cache.put(SERVICE_PATTERNS_KEY, patterns);
    }
    
    return patterns;
  }
  
  private List<Pattern> fetchServicePatterns() {
    String servicesResponse = getUrlContents(SERVICES_URI);
    if (servicesResponse == null) {
      return Collections.emptyList();
    }
    
    try {
      JSONArray servicesJson = new JSONArray(servicesResponse);
      List<Pattern> patterns = Lists.newArrayList();
      for (int i = 0; i < servicesJson.length(); i++) {
        JSONObject serviceJson = servicesJson.getJSONObject(i);
        JSONArray serviceRegexesJson = serviceJson.optJSONArray("regex");
        for (int j = 0; j < serviceRegexesJson.length(); j++) {
          patterns.add(Pattern.compile(serviceRegexesJson.getString(j)));
        }
      }
      return patterns;
    } catch (JSONException err) {
      logger.log(Level.WARNING, "Could not parse services response", err);      
    }
    
    return Collections.emptyList();
  }
  
  private static String getUrlContents(String url) {
    try {
      HTTPRequest request;
          request = new HTTPRequest(new URL(url), HTTPMethod.GET, FETCH_OPTIONS);
      HTTPResponse response = URL_FETCH_SERVICE.fetch(request);
      
      if (response.getResponseCode() >= 300) {
        logger.warning(response.getResponseCode() +
            " response when fetching " + url + " from Embedly");
        return null;
      }

      return new String(response.getContent(), Charset.forName("UTF-8"));
    } catch (MalformedURLException er) {
      logger.warning("Malformed URL: " + url);
    } catch (IOException err) {
      logger.log(Level.WARNING, "Could not fetch: " + url, err); 
    }
    
    return null;
  }

}
