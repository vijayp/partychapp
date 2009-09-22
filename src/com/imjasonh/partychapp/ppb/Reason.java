/**
 * 
 */
package com.imjasonh.partychapp.ppb;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.xmpp.JID;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.ppb.PlusPlusBot.Action;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Reason implements Serializable {
  // TODO(nsanch): where is this number supposed to come from? I just mashed my keys.
  private static final long serialVersionUID = 1874329879824724L;
  
  @SuppressWarnings("unused")
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  @PrimaryKey
  private Key key;
  
  @NotPersistent
  private Target target;
  @Persistent
  private String targetId;

  @NotPersistent
  private Member sender;
  @Persistent
  private String senderJID;
  
  @Persistent
  private String reason;

  @Persistent
  private String action;

  @Persistent
  private Date timestamp;
  
  @Persistent
  private int scoreAfter;

  // This should only be called by Target.takeAction()
  Reason(Target t, Member sender, Action act, String reason, int scoreAfter) {
    this.target = t;
    this.targetId = t.key();
    this.sender = sender;
    this.senderJID = sender.getJID();
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
    if (target == null) {
      target = Datastore.instance().getTargetByID(targetId);
    }
    return target;
  }

  public String reason() {
    return this.reason;
  }
  
  public int scoreAfter() {
    return scoreAfter;
  }
  
  public Member sender() {
    if (sender == null) {
      sender = target().channel().getMemberByJID(new JID(senderJID));
    }
    return sender;
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(action().ifPlusPlusElse("woot!", "ouch!"));
    sb.append(target.name());
    sb.append(" -> ");
    sb.append(scoreAfter);
    return sb.toString();
  }
  
  public void put() {
    Datastore.instance().put(this);
  }
  
  public Reason undo() {
    return target().takeAction(sender(),
                               action().opposite(),
                               "Undo: " + reason());
  }
  
  public String wootString() {
    StringBuilder sb = new StringBuilder();
    sb.append(action().isPlusPlus() ? "[woot! " : "[ouch! ");
    sb.append("now at " + scoreAfter() + "]");
    return sb.toString();
  }
}