package com.imjasonh.partychapp.server.command;

import com.google.appengine.api.urlfetch.FetchOptions;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.ResponseTooLargeException;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;

import com.imjasonh.partychapp.Message;

import org.apache.commons.lang.StringEscapeUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Command that makes sharing of URLs slightly friendlier (looks up titles).
 * 
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class ShareHandler extends SlashCommand {
  private static final Pattern TITLE_START = Pattern.compile("<title[^>]*>");
  private static final String TITLE_END = "</title>";
  
  private static final Logger logger = Logger.getLogger(BugHandler.class.getName());

  public ShareHandler() {
    super("share");
  }
  
  @Override
  void doCommand(Message msg, String argument) {
    if (Strings.isNullOrEmpty(argument)) {
      msg.channel.sendDirect("No URL to share given.", msg.member);
      return;
    }
    
    String[] pieces = argument.split("\\s+", 2);
    
    URI uri;
    try {
      uri = new URI(pieces[0]);
    } catch (URISyntaxException err) {
      msg.channel.sendDirect("Invalid URL to share given.", msg.member);
      return;
    }
    
    if (!uri.isAbsolute()) {
      msg.channel.sendDirect("URLs to share must be absolute", msg.member);
      return;      
    }
    
    String annotation = null;
    if (pieces.length == 2) {
      annotation = pieces[1];
    }
    
    String title = extractPageTitle(uri);
    
    String shareBroadcast = "_" + msg.member.getAlias() + " is sharing " + uri;
    
    if (!Strings.isNullOrEmpty(title)) {
      shareBroadcast += " (" + title + ")";
    }
    
    if (!Strings.isNullOrEmpty(annotation)) {
      shareBroadcast += " : " + annotation;
    }
    
    shareBroadcast += "_";
    msg.channel.broadcastIncludingSender(shareBroadcast);
  }
  
  private String extractPageTitle(URI uri) {
    String content = getUriContents(uri);
    if (Strings.isNullOrEmpty(content)) {
      return null;
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
        return title;
      }
    }
    
    return null;
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
      logger.warning("Malformed URL in share: " + uri);
    } catch (IOException err) {
      // Ignore, don't care if we can't fetch the URL
    } catch (ResponseTooLargeException err) {
      // Shouldn't happen, since we allow truncation, but just in case.
    }
    
    // Fallthrough
    return null;
  }

  public String documentation() {
    return "/share http://example.com/ [annotation] - " +
        "shares a URL with the room";
  }
}
