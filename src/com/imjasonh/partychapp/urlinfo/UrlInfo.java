package com.imjasonh.partychapp.urlinfo;

import com.google.appengine.repackaged.com.google.common.base.Strings;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;

public class UrlInfo {
  public static final UrlInfo EMPTY = new UrlInfo("", "");
  
  private final String title;
  private final String description;
  
  @VisibleForTesting public UrlInfo(String title, String description) {
    this.title = title;
    this.description = description;
  }
  
  public boolean hasTitle() {
    return !Strings.isNullOrEmpty(title);
  }
  
  public String getTitle() {
    return title;
  }
  
  public boolean hasDescription() {
    return !Strings.isNullOrEmpty(description);
  }
    
  public String getDescription() {
    return description;
  }
  
  @Override public boolean equals(Object obj) {
    if (!(obj instanceof UrlInfo)) {
      return false;
    }
    
    UrlInfo o = (UrlInfo) obj;
    return Objects.equal(title, o.title) &&
        Objects.equal(description, o.description);
  }
  
  @Override public int hashCode() {
    return Objects.hashCode(title, description);
  }
}
