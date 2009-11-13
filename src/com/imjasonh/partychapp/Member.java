package com.imjasonh.partychapp;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.xmpp.JID;
import com.google.appengine.repackaged.com.google.common.collect.Lists;

@PersistenceCapable
public class Member implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 8243978327905416562L;

  // private static final Logger LOG = Logger.getLogger(Member.class.getName());

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

  public enum SnoozeStatus {
    SNOOZING,
    NOT_SNOOZING,
    SHOULD_WAKE;
  }

  public Member(Channel c, JID jid) {
    this.jid = jid.getId().split("/")[0]; // remove anything after "/"
    this.alias = this.jid.split("@")[0]; // remove anything after "@" for default alias
    this.debugOptions = new DebuggingOptions();
    this.channel = c;
  }
  
  public Member(Member other) {
    this.channel = other.channel;
    this.key = other.key;
    this.jid = other.jid;
    this.alias = other.alias;
    this.snoozeUntil = other.snoozeUntil;
    if (other.lastMessages != null) {
      this.lastMessages = Lists.newArrayList(other.lastMessages);
    }
    this.debugOptions = new DebuggingOptions(other.debugOptions());
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

  public String getJID() {
    return jid;
  }
  
  public String getEmail() {
    // TODO(nsanch): this isn't quite right because it's possible to have a
    // jabber account that doesn't accept email, but this is good enough until
    // we have a web UI for this.
    return jid;
  }
  
  public String phoneNumber() {
    return phoneNumber;
  }
  
  public void setPhoneNumber(String phone) {
    phoneNumber = phone;
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
  }

  public DebuggingOptions debugOptions() {
    return debugOptions;
  }

  public boolean fixUp(Channel c) {
    boolean shouldPut = false;
    if (channel != c) {
      channel = c;
      //shouldPut = true;
    }
    if (debugOptions == null) {
      debugOptions = new DebuggingOptions();
      shouldPut = true;
    }
    if (lastMessages == null) {
      lastMessages = Lists.newArrayList();
      shouldPut = true;
    }
    return shouldPut;
  }
  
  public void put() {
    channel.put();
  }

  public Key key() {
    return key;
  }
}
