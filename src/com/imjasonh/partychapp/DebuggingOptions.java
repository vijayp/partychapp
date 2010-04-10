package com.imjasonh.partychapp;

import java.io.Serializable;
import java.util.List;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.google.appengine.repackaged.com.google.common.collect.Lists;

@PersistenceCapable
public class DebuggingOptions implements Serializable {
  private static final long serialVersionUID = 98432798750987435L;

  @Persistent
  private List<String> opts = Lists.newArrayList();
  
  public DebuggingOptions() {}
  public DebuggingOptions(DebuggingOptions other) {
    this.opts = Lists.newArrayList(other.opts);
  }

  public void add(String opt) {
    opts.add(opt);
  }
  
  public void clear() {
    opts.clear();
  }

  @Override
  public String toString() {
    return opts.toString();
  }
  
  public boolean isEnabled(String opt) {
    return opts.contains(opt);
  }
}
