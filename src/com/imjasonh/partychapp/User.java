package com.imjasonh.partychapp;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.JDOHelper;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class User implements Serializable {

  private static final long serialVersionUID = 89437432538532985L;

  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(User.class.getName());

  /**
   * How often the lastSeen field should be updated (it's only used for
   * computing daily active stats, so it's wasteful to cause writes for every
   * single message received). For now only update it every 12 hours.
   */
  private static final long LAST_SEEN_UPDATE_INTERNAL_MS =
      12L * 60L * 60L * 1000L;

  @Persistent
  @PrimaryKey
  private String jid;

  @Persistent
  List<String> channelNames;
  
  @Persistent
  String phoneNumber;
  
  @Persistent
  String carrier;
  
  @Persistent
  Date lastSeen;

  // I stole from http://en.wikipedia.org/wiki/List_of_carriers_providing_SMS_transit
  public enum Carrier {
    ATT("at&t", "txt.att.net", false),
    VERIZON("verizon", "vtext.net", true),
    TMOBILE("tmobile", "tmomail.net", true),
    SPRINT("sprint", "messaging.sprintpcs.com", true),
    VIRGIN("virgin", "vmobl.com", true)
    ;

    public final String shortName;
    public final String emailToSmsDomain;
    public final boolean wantsLeadingOne;

    private Carrier(String shortName, String emailToSmsDomain, boolean wantsLeadingOne) {
      this.shortName = shortName;
      this.emailToSmsDomain = emailToSmsDomain;
      this.wantsLeadingOne = wantsLeadingOne;
    }

    public String emailAddress(String phoneNumber) {
      if (phoneNumber == null) {
        return null;
      }
      if (wantsLeadingOne) {
        if (!phoneNumber.startsWith("1")) {
          phoneNumber = "1" + phoneNumber;
        }
      } else {
        if (phoneNumber.startsWith("1")) {
          phoneNumber = phoneNumber.substring(1);
        }
      }
      return phoneNumber + "@" + emailToSmsDomain;
    }
  }

  public User(String jid) {
    this.jid = jid;
    this.channelNames = Lists.newArrayList();
  }

  public User(User other) { 
    this.channelNames = other.channelNames;
    this.jid = other.jid;
    this.phoneNumber = other.phoneNumber;
    this.carrier = other.carrier;
    this.lastSeen = other.lastSeen;
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
  
  public User.Carrier carrier() {
    if (carrier == null) {
      return null;
    } 
    return Carrier.valueOf(carrier);
  }
  
  public void setCarrier(Carrier carrier) {
    this.carrier = carrier.name();
  }

  public boolean canReceiveSMS() {
    return carrier != null && phoneNumber != null;
  }

  public Date lastSeen() {
    return lastSeen;
  }
  
  public void maybeMarkAsSeen() {
    if (lastSeen == null ||
        (new Date().getTime() - lastSeen().getTime() > 
            User.LAST_SEEN_UPDATE_INTERNAL_MS)) {
      lastSeen = new Date();
      put();
    }    
  }  

  public List<String> channelNames() {
    return Collections.unmodifiableList(channelNames);
  }
  
  /**
   * Gets all of the channels the user is actually in (and which exist).
   */
  public List<Channel> getChannels() {
    boolean shouldPut = false;
    
    List<Channel> channels =
        Lists.newArrayListWithExpectedSize(channelNames.size());
    
    for (String channelName : channelNames) {
      Channel channel = Datastore.instance().getChannelByName(channelName);
      if (channel != null) {
        if (channel.getMemberByJID(jid) != null) {
          channels.add(channel);
        }
      } else {
        logger.warning(
            "User " + jid + " was in non-existent channel " + channelName +
            ", removing");
        removeChannel(channelName);
        shouldPut = true;
      }
    }
    
    // While we have all these channels loaded, also take the opportunity to
    // to do other fixUps
    for (Channel channel : channels) {
      fixUp(channel);
    }
    
    if (shouldPut) {
      put();
    }
    
    return channels;
  }
  
  public void put() {
    Datastore.instance().put(this);
  }  
  
  @Override public String toString() {
    return "[User: jid: " + jid + ", phoneNumber: " + phoneNumber +
      ", carrier: " + carrier + ", channelNames: " + channelNames +
      "]";
  }  
  
  /**
   * Makes sure that the user <-> channel relationship is consistent.
   * 
   * @param channel channel that the user is IM-ing (and may be in). It is
   * considered the source of truth, the user object will be updated based on
   * its members list.
   */
  public void fixUp(Channel channel) {
    boolean shouldPut = false;
    
    String channelName = channel.getName();
    if (channel.getMemberByJID(jid) == null &&
        channelNames.contains(channelName)) {
      logger.warning(
          "User " + jid + " wasn't actually in " + channelName + ", removing");
      removeChannel(channelName);
      shouldPut = true;
    }
    
    if (channel.getMemberByJID(jid) != null &&
        !channelNames.contains(channelName)) {
      logger.warning(
          "User " + jid + " was supposed to be in " + channelName + ", adding");
      addChannel(channelName);
      shouldPut = true;
    }
    
    if (shouldPut) {
      put();
    }
  }  
  
  // The remaining methods deal with manipulation of the User/Channel 
  // relationship and should called by {@link Channel} and {@link Datastore} 
  // implementations only.
   
  @VisibleForTesting public void addChannel(String c) {
    if (!channelNames.contains(c)) {
      channelNames.add(c);
      
      // I feel dirty doing this! There is some opaque JDO bug that makes
      // this not save.
      JDOHelper.makeDirty(this, "channelNames");
    }
  }
  
  @VisibleForTesting public void removeChannel(String c) {
    if (channelNames.contains(c)) {
      channelNames.remove(c);
      
      // Similar to the dirty hack in {@link #addChannel}
      JDOHelper.makeDirty(this, "channelNames");
    }
  }
}
