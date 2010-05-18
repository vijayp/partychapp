package com.imjasonh.partychapp;

import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.List;

public class DebuggingOptions implements Serializable {
  private static final long serialVersionUID = 98432798750987435L;
  
  public static enum Option {
    /** 
     * Include a sequence ID with every message sent, so that dropped messages 
     * can be spotted.
     */
    SEQUENCE_IDS("sequenceIds"),
    /**
     * Notify the member if a message sent to them resulted in an RPC error, so 
     * that they can check if they actually got it.
     */
    ERROR_NOTIFICATIONS("errorNotifications");
    
    private final String id;
    
    Option(String id) {
      this.id = id;
    }
    
    @Override public String toString() {
      return id;
    }
    
    public static Option fromString(String id) {
      for (Option option : values()) {
        if (option.id.equals(id)) {
          return option;
        }
      }
      
      return null;
    }
  }

  private List<String> opts = Lists.newArrayList();
  
  public DebuggingOptions() {}
  
  public DebuggingOptions(DebuggingOptions other) {
    this.opts = Lists.newArrayList(other.opts);
  }

  public void add(Option option) {
    opts.add(option.toString());
  }
  
  public void clear() {
    opts.clear();
  }

  @Override
  public String toString() {
    return opts.toString();
  }
  
  public boolean isEnabled(Option option) {
    return opts.contains(option.toString());
  }
}
