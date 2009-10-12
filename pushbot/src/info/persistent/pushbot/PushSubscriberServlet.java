package info.persistent.pushbot;

import com.google.appengine.api.xmpp.JID;
import com.google.appengine.repackaged.com.google.common.base.StringUtil;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

import info.persistent.pushbot.util.Xmpp;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class PushSubscriberServlet extends HttpServlet {
  private static final Logger logger =
      Logger.getLogger(PushSubscriberServlet.class.getName());
  
  private static final int MAX_ENTRIES_TO_DISPLAY = 3;

  /** Subscription verifications arrive via GETs */
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    resp.setStatus(200);
    resp.setContentType("text/plain");
    resp.getOutputStream().print(req.getParameter("hub.challenge"));
    resp.getOutputStream().flush();

    JID user = new JID(req.getPathInfo().substring(1));

    if (req.getParameter("hub.mode").equals("subscribe")) {
      Xmpp.sendMessage(user, "Subscribed to " + req.getParameter("hub.topic"));
    } else if (req.getParameter("hub.mode").equals("unsubscribe")) {
      Xmpp.sendMessage(
          user, "Unsubscribed from " + req.getParameter("hub.topic"));      
    }
  }

  /** Actual notifications arrive via POSTs */
  @SuppressWarnings("unchecked")
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    resp.setStatus(204);

    SyndFeedInput input = new SyndFeedInput();
    SyndFeed feed;
    try {
      feed = input.build(new XmlReader(req.getInputStream()));
    } catch (IllegalArgumentException err) {
      logger.log(Level.INFO, "Feed parse error", err);
      return;
    } catch (FeedException err) {
      logger.log(Level.INFO, "Feed parse error", err);
      return;
    }
    
    
    List<SyndEntry> entries = feed.getEntries();
    
    if (entries.isEmpty()) {
      return;
    }
 
    // If subscribing to a previously unseen URL, the hub might report a bunch
    // of entries as new, so we sort them by published date and only show the
    // first few
    Collections.sort(entries, new Comparator<SyndEntry>() {
      @Override public int compare(SyndEntry o1, SyndEntry o2) {
        return o2.getPublishedDate().compareTo(o1.getPublishedDate());
      }
    });
    
    List<SyndEntry> displayEntries;
    if (entries.size() > MAX_ENTRIES_TO_DISPLAY) {
      displayEntries = entries.subList(0, MAX_ENTRIES_TO_DISPLAY);
    } else {
      displayEntries = entries;
    }
    
    StringBuilder message = new StringBuilder("Update from ")
        .append(StringUtil.unescapeHTML(feed.getTitle())).append(":");
    for (SyndEntry displayEntry : displayEntries) {
      message.append("\n  ")
          .append(StringUtil.unescapeHTML(displayEntry.getTitle()))
          .append(": ")
          .append(displayEntry.getLink());
    }
    
    if (displayEntries.size() != entries.size()) {
      message.append("\n  (and ")
        .append(entries.size() - displayEntries.size()).append(" more)");
    }
    
    JID user = new JID(req.getPathInfo().substring(1));
    Xmpp.sendMessage(user, message.toString());
  }
}
