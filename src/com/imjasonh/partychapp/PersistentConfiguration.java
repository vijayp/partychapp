package com.imjasonh.partychapp;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Used to configure private information that shouldn't be checked into svn. At
 * the moment, this is just the session token and feed URL for updating the
 * stats spreadsheet we keep in google docs, but it could grow to include other
 * things too.
 * 
 * There's no facility for updating this from code. You have to go into the
 * datastore admin interface and edit the row there.
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
  
  @Persistent
  private String sessionToken;
  
  @Persistent
  private String listFeedUrl;
  
  public String sessionToken() { return sessionToken; }
  public String listFeedUrl() { return listFeedUrl; }
}
