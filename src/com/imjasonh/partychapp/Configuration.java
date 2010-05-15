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
  
  public static PersistentConfiguration persistentConfig() {
    if (pc == null) {
      pc = Datastore.instance().getPersistentConfig();
    }
    return pc;
  }
}
