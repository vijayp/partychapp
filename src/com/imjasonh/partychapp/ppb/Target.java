/**
 * 
 */
package com.imjasonh.partychapp.ppb;

import java.io.Serializable;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.ppb.PlusPlusBot.Action;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Target implements Serializable {
  private static final long serialVersionUID = 32874987598375313L;
  
  @Persistent
  @PrimaryKey
  private String key; 
 
  @NotPersistent
  private Channel channel;
  @Persistent
  private String channelName;

  @Persistent
  private String name;

  @Persistent
  private int score;

  public Reason takeAction(Member sender, Action act, String content) {
    if (act.isPlusPlus()) {
      ++score;
    } else {
      --score;
    }
    return new Reason(this, sender, act, content, score);
  } 

  public Target(String name, Channel channel) {
    this.key = Target.createTargetKey(name, channel);
    this.name = name;
    this.channel = channel;
    this.channelName = channel.getName();
    this.score = 0;
  }

  public Target(Target other) {
    this.key = other.key;
    this.name = other.name;
    this.channel = other.channel;
    this.channelName = other.channelName;
    this.score = other.score;
  }
  
  public String key() {
    return key;
  }

  public String name() {
    return name;
  }
  
  public int score() {
    return score;
  }
  
  public void put() {
    Datastore.instance().put(this);
  }

  public Channel channel() {
    if (channel == null) {
      channel = Datastore.instance().getChannelByName(channelName);
    }
    return channel;
  }
  
  public static String createTargetKey(String name, Channel channel) {
    return "Channel: " + channel.getName() + ", Name: " + name;
  }
  
  public String toString() {
    return "Target: [Key: [" + key() + "], score: " + score + "]";
  }
}