package com.imjasonh.partychapp;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Used to configure private, per-installation or frequently changed information
 * that shouldn't be checked into SVN. Can be viewed and edited at the
 * /admin/config URL.
 *
 * @author nsanch
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class PersistentConfiguration {
  private static final long serialVersionUID = 7984372525340987L;

  @SuppressWarnings("unused")
  @PrimaryKey
  @Persistent
  private String name = "config";
  
  /** AuthSub session token for updating the stats spreadsheet */
  @Persistent
  private String sessionToken;
  
  /** GData feed URL for the stats spreadsheet */
  @Persistent
  private String listFeedUrl;
  
  /** Whether channel stats are being recorded or not (has overhead) */
  @Persistent
  private Boolean areChannelStatsEnabled;
  
  public String sessionToken() { return sessionToken; }
  public String listFeedUrl() { return listFeedUrl; }
  public boolean areChannelStatsEnabled() {
    return areChannelStatsEnabled != null &&
        areChannelStatsEnabled.booleanValue(); 
  }
  
  // Setters are meant for use by {@link ConfigEditServlet} only
  
  public void setSessionToken(String sessionToken) {
    this.sessionToken = sessionToken;
  }
  
  public void setListFeedUrl(String listFeedUrl) {
    this.listFeedUrl = listFeedUrl;
  }
  
  public void setChannelStatsEnabled(boolean areChannelStatsEnabled) {
    this.areChannelStatsEnabled = areChannelStatsEnabled;
  }
}
