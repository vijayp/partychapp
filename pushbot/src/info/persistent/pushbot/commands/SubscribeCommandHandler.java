package info.persistent.pushbot.commands;

import com.google.appengine.api.xmpp.JID;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndLink;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

import org.jdom.Element;

import info.persistent.pushbot.Subscription;
import info.persistent.pushbot.util.Hubs;
import info.persistent.pushbot.util.Persistence;
import info.persistent.pushbot.util.Xmpp;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

public class SubscribeCommandHandler extends FeedCommandHandler {
  private static final Logger logger =
      Logger.getLogger(SubscribeCommandHandler.class.getName());
  
  private static final String HUB_RELATION = "hub";  
  private static final String SELF_RELATION = "self";
  
  private static final String ATOM_NAMESPACE = "http://www.w3.org/2005/Atom";
  private static final String ATOM_LINK = "link";
  private static final String ATOM_REL_ATTRIBUTE = "rel";
  private static final String ATOM_HREF_ATTRIBUTE = "href";

  @Override
  protected void handle(JID user, URL feedUrl) {
    subscribeToFeed(user, feedUrl);
  }
  
  static void subscribeToFeed(JID user, URL feedUrl) {
    SyndFeed feed = fetchAndParseFeed(user, feedUrl);
    if (feed == null) {
      return;
    }

    URL hubUrl = getLinkUrl(feed, HUB_RELATION);
    if (hubUrl == null || hubUrl.getHost() == null
        || hubUrl.getHost().isEmpty()) {
      Xmpp.sendMessage(
          user, "The feed " + feedUrl + " is not associated with a hub");
      return;
    }
    
    // If possible, subscribe to the self URL, since presumably that's the one
    // that the hub knows about.
    URL selfUrl = getLinkUrl(feed, SELF_RELATION);
    if (selfUrl != null) {
      feedUrl = selfUrl;
    }

    saveSubscription(user, feedUrl, hubUrl, feed.getTitle());

    Hubs.sendRequestToHub(user, hubUrl, feedUrl, true);
  }

  private static SyndFeed fetchAndParseFeed(JID user, URL feedUrl) {
    SyndFeedInput feedInput = new SyndFeedInput();
    try {
      return feedInput.build(new XmlReader(feedUrl));
    } catch (IllegalArgumentException e) {
      handleFeedParseException(user, feedUrl, e);
      return null;
    } catch (FeedException e) {
      handleFeedParseException(user, feedUrl, e);
      return null;
    } catch (IOException e) {
      handleFeedParseException(user, feedUrl, e);
      return null;
    } catch (IllegalStateException e) {
      handleFeedParseException(user, feedUrl, e);
      return null;
    }
  }

  private static void handleFeedParseException(
      JID user, URL feedUrl, Throwable t) {
    logger.log(Level.INFO, "Feed parse exception for " + feedUrl, t);
    Xmpp.sendMessage(user, "Could not parse feed " + feedUrl);
  }

  @SuppressWarnings("unchecked")
  private static URL getLinkUrl(SyndFeed feed, String relation) {
    // Atom feeds can have links accessed directly.
    for (SyndLink link : ((List<SyndLink>) feed.getLinks())) {
      if (link.getRel().equals(relation)) {
        try {
          return new URL(link.getHref());
        } catch (MalformedURLException err) {
          logger.log(Level.INFO, "Malformed " + relation + " URL", err);
          return null;
        }
      }
    }
    
    // If we have an Atom 1.0 <link> in an RSS feed, it's in the foreign markup
    // list.
    List<Element> elements = (List<Element>) feed.getForeignMarkup();
    for (Element element : elements) {
      if (element.getNamespaceURI().equals(ATOM_NAMESPACE) &&
          element.getName().equals(ATOM_LINK) &&
          relation.equals(element.getAttributeValue(ATOM_REL_ATTRIBUTE))) {
        String href = element.getAttributeValue(ATOM_HREF_ATTRIBUTE);
        if (href != null && !href.isEmpty()) {
          try {
            return new URL(href);
          } catch (MalformedURLException err) {
            logger.log(Level.INFO, "Malformed " + relation + " URL", err);
            return null;
          }
        }
      }
    }

    return null;
  }
  
  private static void saveSubscription(
      JID user, URL feedUrl, URL hubUrl, String title) {
    List<Subscription> existingSubscriptions =
      Subscription.getSubscriptionsForUserAndFeedUrl(user, feedUrl);
    if (!existingSubscriptions.isEmpty()) {
      Xmpp.sendMessage(user, "You're already subscribed to " + feedUrl +
          " (sending request to hub anyway, in case it's out of sync)");
      return;
    }
    
    final Subscription subscription =
      new Subscription(user, feedUrl, hubUrl, title);
    Persistence.withManager(new Persistence.Closure() {
      @Override
      public void run(PersistenceManager manager) {
        manager.makePersistent(subscription);
      }
    });
  }

}
