package com.imjasonh.partychapp.server.command;

import com.google.common.base.Strings;

import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.urlinfo.UrlInfo;
import com.imjasonh.partychapp.urlinfo.UrlInfoService;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Command that makes sharing of URLs slightly friendlier (looks up titles).
 * 
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class ShareHandler extends SlashCommand {
  private final UrlInfoService urlInfoService;

  public ShareHandler(UrlInfoService urlInfoService) {
    super("share");
    this.urlInfoService = urlInfoService;
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
    
    String shareBroadcast = "_" + msg.member.getAlias() + " is sharing " + uri;
    
    UrlInfo urlInfo = urlInfoService.getUrlInfo(uri);
    if (urlInfo.hasTitle()) {
      shareBroadcast += " (" + urlInfo.getTitle() + ")";
    }
    
    if (!Strings.isNullOrEmpty(annotation)) {
      shareBroadcast += " : " + annotation;
    }
    
    shareBroadcast += "_";

    if (urlInfo.hasDescription()) {
      shareBroadcast += "\n  " + urlInfo.getDescription();
    }

    msg.channel.broadcastIncludingSender(shareBroadcast);
  }
  
  public String documentation() {
    return "/share http://example.com/ [annotation] - " +
        "shares a URL with the room";
  }
}
