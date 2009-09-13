/**
 * 
 */
package com.imjasonh.partychapp.ppb;

import javax.jdo.annotations.Persistent;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.ppb.PlusPlusBot.Action;

// @PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Target {
  // TODO(nsanch): what annotation or relationship should I use?
  Channel channel;

  @Persistent
  private String name;

  @Persistent
  private int score;

  public Reason takeAction(Action act, String content) {
    if (act == Action.PLUSPLUS) {
      ++score;
    } else {
      --score;
    }
    Reason r = new Reason(this, act, content, score);
    r.put();
    return r;
  }
  
  public Target(String name, Channel channel) {
    this.name = name;
    this.channel = channel;
    this.score = 0;
  }

  public String name() {
    return name;
  }
  
  public int score() {
    return score;
  }
}