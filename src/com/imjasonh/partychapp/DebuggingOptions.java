package com.imjasonh.partychapp;

import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.List;

public class DebuggingOptions implements Serializable {
  private static final long serialVersionUID = 98432798750987435L;

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
