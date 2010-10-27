package com.imjasonh.partychapp;

public class Configuration {
//  public static final String chatDomain = "partychat-nsanch.appspotchat.com";
//  public static final String webDomain = "partychat-nsanch.appspot.com";
//  public static final String mailDomain = "partychat-nsanch.appspotmail.com";

  public static final String chatDomain = "partychapp.appspotchat.com";
  public static final String webDomain = "partychapp.appspot.com";
  public static final String mailDomain = "partychapp.appspotmail.com";

  public static final boolean isConfidential = false;
  
  // A nightly stats email is sent to this address. (See StatsCronJob.)
  public static final String statsEmailAddress = "nsanch@gmail.com";
  
  //public static final String chatDomain = "partych.at";
  //public static final String webDomain = "partych.at";
  
  private static PersistentConfiguration pc;
  private static long pcLoadTimeMillis = 0;
  private static final long MAX_PC_AGE_MILLIS = 60 * 60 * 1000L;
  
  public static void reloadPersistentConfig() {
    pc = null;
    persistentConfig();
  }
  
  public static PersistentConfiguration persistentConfig() {
    long now = System.currentTimeMillis();
    long pcAge = now - pcLoadTimeMillis;
    if (pc == null || pcAge > MAX_PC_AGE_MILLIS) {
      pcLoadTimeMillis = now;
      pc = Datastore.instance().getPersistentConfig();
      if (pc == null) {
        pc = new PersistentConfiguration();
      }
    }
    return pc;
  }
}
