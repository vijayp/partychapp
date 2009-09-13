/**
 * 
 */
package com.imjasonh.partychapp.ppb;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.ppb.PlusPlusBot.Action;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Reason implements Serializable {
  // TODO(nsanch): where is this number supposed to come from? I just mashed my keys.
  private static final long serialVersionUID = 1874329879824724L;
  
  @SuppressWarnings("unused")
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  @PrimaryKey
  private Key key;
  
  // TODO(nsanch): what do I do for this?
  private Target target;

  @Persistent
  private String reason;

  @Persistent
  private String action;

  @Persistent
  private Date timestamp;
  
  @Persistent
  private int scoreAfter;

  public Reason(Target t, Action act, String reason, int scoreAfter) {
    this.target = t;
    this.action = act.name();
    this.reason = reason;
    this.timestamp = new Date();
    this.scoreAfter = scoreAfter;
  }

  public Action action() {
    return Action.valueOf(action);
  }

  public Date timestamp() {
    return this.timestamp;
  }

  public Target target() {
    return this.target;
  }

  public String reason() {
    return this.reason;
  }
  
  public int scoreAfter() {
    return scoreAfter;
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append((action() == Action.PLUSPLUS) ? "woot!" : "ouch! ");
    sb.append(target.name());
    sb.append(" -> ");
    sb.append(scoreAfter);
    return sb.toString();
  }
  
  public void put() {
    Datastore.get().put(this);
  }
}