package com.imjasonh.partychapp.testing;

import com.google.apphosting.api.ApiProxy;

import com.imjasonh.partychapp.Datastore;

import java.io.Serializable;
import java.util.Collection;

/**
 * {@link Datastore} implementation that can be used to simulate App Engine
 * read-only mode.
 * 
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class ReadOnlyFakeDatastore extends FakeDatastore {
  private boolean isReadOnly = true;
  
  public void setReadOnly(boolean isReadOnly) {
    this.isReadOnly = isReadOnly;
  }
  
  @Override public void setUp() {
    boolean wasReadOnly = isReadOnly;
    isReadOnly = false;
    super.setUp();
    isReadOnly = wasReadOnly;
  }
  
  @Override public void put(Serializable s) {
    if (isReadOnly) {
      // Per http://code.google.com/appengine/docs/java/howto/maintenance.html
      // this is the exception that's thrown in read-only mode
      throw new ApiProxy.CapabilityDisabledException("", "");
    } else {
      super.put(s);
    }
  }
  
  @Override public void putAll(Collection<? extends Serializable> objects) {
    if (isReadOnly) {
      throw new ApiProxy.CapabilityDisabledException("", "");
    } else {
      super.putAll(objects);
    }
  }
  
  @Override public void delete(Serializable s) {
    if (isReadOnly) {
      throw new ApiProxy.CapabilityDisabledException("", "");
    } else {
      super.delete(s);
    }
  }  
}
