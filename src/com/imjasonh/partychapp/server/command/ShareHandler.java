package com.imjasonh.partychapp.server.command;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import com.imjasonh.partychapp.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Command that makes sharing of URLs slightly friendlier (looks up titles).
 * 
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class ShareHandler extends SlashCommand {
  private static final String TITLE_START = "<title>";
  private static final String TITLE_END = "</title>";
  
  // TODO(mihaip): do we care about other HTML entities?
  private static final Map<Pattern, String> HTML_ENTITIES = ImmutableMap.of(
      Pattern.compile(Pattern.quote("&lt;")), "<",
      Pattern.compile(Pattern.quote("&gt;")), ">",
      Pattern.compile(Pattern.quote("&amp;")), "&"
  );
  
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

    int titleStart = contentLower.indexOf(TITLE_START);
    if (titleStart != -1) {
      int titleEnd = contentLower.indexOf(TITLE_END, titleStart);
      if (titleEnd != -1) {
        String title =
            content.substring(titleStart + TITLE_START.length(), titleEnd);
        title = unescapeHtml(title);
        // Normalize newlines and other whitespace
        title = title.replaceAll("\\s+", " ").trim();
        return title;
      }
    }
    
    return null;
  }
  
  @VisibleForTesting static String unescapeHtml(String html) {
    for (Map.Entry<Pattern, String> entry : HTML_ENTITIES.entrySet()) {
      html = entry.getKey().matcher(html).replaceAll(entry.getValue());
    }
    
    return html;
  }
  
  @VisibleForTesting protected String getUriContents(URI uri) {
    try {
      BufferedReader reader = 
        new BufferedReader(new InputStreamReader(uri.toURL().openStream()));
      String content = "";
      String line;
      while ((line = reader.readLine()) != null) {
        content += line + "\n";
      }
      reader.close();
      return content;
    } catch (MalformedURLException err) {
      // Ignore, but all URIs should be URLs
      logger.warning("Malformed URL in share: " + uri);
    } catch (IOException err) {
      // Ignore, don't care if we can't fetch the URL
    }
    
    // Fallthrough
    return null;
  }

  public String documentation() {
    return "/share http://example.com/ [annotation] - " +
        "shares a URL with the room";
  }
}
