package info.persistent.pushbot.data;

import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.xmpp.JID;
import com.google.common.collect.Lists;

import info.persistent.pushbot.util.Persistence;
import info.persistent.pushbot.util.Xmpp;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(
    identityType = IdentityType.APPLICATION, detachable = "true")
public class Subscription {
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long id;
  
  @Persistent
  private String user;
  
  @Persistent(defaultFetchGroup = "true")
  private Link feedUrl;
  
  @Persistent(defaultFetchGroup = "true")
  private Link hubUrl;
  
  @Persistent
  private String title;
  
  public Subscription(JID user, URL feedUrl, URL hubUrl, String title) {
    this.user = Xmpp.toShortJid(user).getId();
    this.feedUrl = new Link(feedUrl.toString());
    this.hubUrl = new Link (hubUrl.toString());
    this.title = title;
  }
  
  public Long getId() {
    return id;
  }
  
  public JID getUser() {
    return new JID(user);
  }
  
  public URL getFeedUrl() {
    try {
      return new URL(feedUrl.getValue());
    } catch (MalformedURLException err) {
      // All stored URLs should be valid, since we're creating them from URL
      // instances
      throw new RuntimeException(err);
    }
  }
  
  public URL getHubUrl() {
    try {
      return new URL(hubUrl.getValue());
    } catch (MalformedURLException err) {
      // Ditto
      throw new RuntimeException(err);
    }
  }
  
  public String getTitle() {
    return title;
  }
  
  public static List<Subscription> getSubscriptionsForUser(JID user) {
    final String queryUserId = Xmpp.toShortJid(user).getId();
    final List<Subscription> result = Lists.newArrayList();
    Persistence.withManager(new Persistence.Closure() {
      @SuppressWarnings("unchecked")
      @Override public void run(PersistenceManager manager) {
        Query query = manager.newQuery(Subscription.class);
        query.setFilter("user == userParam");
        query.declareParameters("String userParam");
        result.addAll((List<Subscription>) query.execute(queryUserId));
        query.closeAll();
      }
    });
    
    return result;
  }
  
  public static List<Subscription> getSubscriptionsForUserAndFeedUrl(
      JID user, URL feedUrl) {
    final String queryUserId = Xmpp.toShortJid(user).getId();
    final Link queryFeedUrl = new Link(feedUrl.toString());
    final List<Subscription> result = Lists.newArrayList();
    Persistence.withManager(new Persistence.Closure() {
      @SuppressWarnings("unchecked")
      @Override public void run(PersistenceManager manager) {
        Query query = manager.newQuery(Subscription.class);
        query.setFilter("user == userParam && feedUrl == feedUrlParam");
        query.declareParameters("String userParam, " +
            "com.google.appengine.api.datastore.Link feedUrlParam");
        result.addAll(
            (List<Subscription>) query.execute(queryUserId, queryFeedUrl));
        query.closeAll();
      }
    });
    
    return result;
  }  
}
