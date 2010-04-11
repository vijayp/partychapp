package com.imjasonh.partychapp;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.common.collect.Lists;

@PersistenceCapable
public class Member implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 8243978327905416562L;

  private static final Logger LOG = Logger.getLogger(Member.class.getName());

  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  @PrimaryKey
  private Key key;

  @Persistent
  private String jid;

  @Persistent
  private String alias;

  @Persistent
  private Date snoozeUntil;
  
  @Persistent(serialized = "true")
  private List<String> lastMessages = Lists.newArrayList();

  @Persistent(serialized = "true")
  private DebuggingOptions debugOptions = new DebuggingOptions();
  
  //@Persistent
  @NotPersistent
  private Channel channel;
  
  @Persistent
  String phoneNumber;
  
  @Persistent
  String carrier;
  
  @NotPersistent
  User user;

  public enum SnoozeStatus {
    SNOOZING,
    NOT_SNOOZING,
    SHOULD_WAKE;
  }
  
  public Member(Channel c, User user) {
    this.user = user;
    this.jid = user.getJID();
    this.alias = this.jid.split("@")[0]; // remove anything after "@" for default alias
    this.debugOptions = new DebuggingOptions();
    this.channel = c;
  }
  
  public Member(Member other) {
    this.key = other.key;
    this.jid = other.jid;
    this.alias = other.alias;
    this.snoozeUntil = other.snoozeUntil;
    if (other.lastMessages != null) {
      this.lastMessages = Lists.newArrayList(other.lastMessages);
    }
    this.debugOptions = new DebuggingOptions(other.debugOptions());
    this.phoneNumber = other.phoneNumber;
    this.carrier = other.carrier;
    // to simulate the not-persistent-ness, let's zero these out
    this.user = null;
    this.channel = null;
  }

  public String getAlias() {
    return alias;
  }
  
  public String getAliasPrefix() {
    return "[" + alias + "] ";
  }

  public void setAlias(String alias) {
    this.alias = alias;
  }
  
  public void setUser(User u) {
    if ((user != null) && (user != u)) {
      LOG.severe("attempt to override existing User object " + user + " with replacement " + u);
    }
    user = u;
  }
  
  public User user() {
    return user;
  }

  public String getJID() {
    return jid;
  }
  
  public String getEmail() {
    return user().getEmail();
  }
  
  public SnoozeStatus getSnoozeStatus() {
    Date now = new Date();
    if (snoozeUntil == null) {
      return SnoozeStatus.NOT_SNOOZING;
    } else {
      if (snoozeUntil.before(now)) {
        return SnoozeStatus.SHOULD_WAKE;
      } else {
        return SnoozeStatus.SNOOZING;
      }
    }
  }

  public void setSnoozeUntil(Date snoozeUntil) {
    this.snoozeUntil = snoozeUntil;
  }

  public Date getSnoozeUntil() {
    return snoozeUntil;
  }
  
  public boolean unsnoozeIfNecessary() {
    if (getSnoozeStatus() == SnoozeStatus.SHOULD_WAKE) {
      setSnoozeUntil(null);
      return true;
    }
    return false;
  }
  
  private List<String> mutableLastMessages() {
    return lastMessages;
  }

  public List<String> getLastMessages() {
    return Collections.unmodifiableList(mutableLastMessages());
  }
  
  public void addToLastMessages(String toAdd) {
    setSnoozeUntil(null);
    List<String> messages = mutableLastMessages();
    messages.add(0, toAdd);
    if (messages.size() > 10) {
      messages.remove(10);
    }
    user().markSeen();
    user().put();
  }

  public DebuggingOptions debugOptions() {
    return debugOptions;
  }

  public boolean fixUp(Channel c) {
    boolean shouldPut = false;
    if (channel != c) {
      channel = c;
    }
    if (debugOptions == null) {
      debugOptions = new DebuggingOptions();
      shouldPut = true;
    }
    if (lastMessages == null) {
      lastMessages = Lists.newArrayList();
      shouldPut = true;
    }
    if (phoneNumber != null) {
      user.setPhoneNumber(phoneNumber);
      phoneNumber = null;
    }
    if (carrier != null) {
      user.setCarrier(User.Carrier.valueOf(carrier));
      carrier = null;
    }
    if (!user.channelNames.contains(channel.getName())) {
      user.addChannel(channel.getName());
      user.put();
      shouldPut = true;
    }
    return shouldPut;
  }
  
  public void put() {
    channel.put();
    user.put();
  }

  public Key key() {
    return key;
  }
  
  public static class SortMembersForListComparator implements Comparator<Member> {
    public int compare(Member first, Member second) {
      // TODO: sort by online/offline, snoozing
      return first.getAlias().compareTo(second.getAlias());
    }
  }
}
