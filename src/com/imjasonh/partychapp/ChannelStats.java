package com.imjasonh.partychapp;

import com.google.appengine.api.xmpp.JID;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheManager;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * In-memory (via memcached) stats about channels. Not persisted in any way.
 *
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class ChannelStats implements Serializable {
  public class ChannelStat {
    private final String channelName;
    private final int byteCount;
    
    private ChannelStat(String channelName, int byteCount) {
      this.channelName = channelName;
      this.byteCount = byteCount;
    }
    
    public String getChannelName() {
      return channelName;
    }
    
    public int getByteCount() {
      return byteCount;
    }
  }
  
  private static final Logger logger =
      Logger.getLogger(ChannelStats.class.getName());
  
  private static Cache CACHE = null;
  static {
    try {
      CACHE = CacheManager.getInstance().getCacheFactory()
          .createCache(Collections.emptyMap());
    } catch (CacheException err) {
      logger.warning("Could not initialize ChannelStats cache");
    }
  }
  
  private static final String STATS_CACHE_KEY = "channel-stats";

  private static final int TOP_CHANNEL_COUNT = 50;  
  
  private final Date creationDate = new Date();
  private Date lastUpdateDate = new Date();
  private final Multiset<String> channelByteCounts = HashMultiset.create();
  
  /**
   * Only {@link #increment} should be creating instances.
   */
  private ChannelStats() {
    // No further initialization needed
  }
  
  private void incrementImpl(JID fromJID, String msg, List<JID> toJIDs) {
    int byteCount = 0;
    
    byteCount += fromJID.toString().length();
    byteCount += msg.length() * toJIDs.size();
    for (JID toJID : toJIDs) {
      byteCount += toJID.toString().length();
    }
    
    String channelName = fromJID.getId().split("@")[0];
    channelByteCounts.add(channelName, byteCount);
    
    lastUpdateDate = new Date();
  }
  
  public Date getCreationDate() {
    return creationDate;
  }
  
  public Date getLastUpdateDate() {
    return lastUpdateDate;
  }
  
  public List<ChannelStat> getTopChannels() {
    // TODO(mihaip): can be more efficient by using a heap
    List<String> channelNames =
        Lists.newArrayList(channelByteCounts.elementSet());
    Collections.sort(channelNames, new Comparator<String>() {
      @Override public int compare(String o1, String o2) {
        return Integer.valueOf(channelByteCounts.count(o2))
            .compareTo(channelByteCounts.count(o1));
      }
    });
    
    List<ChannelStat> stats =
          Lists.newArrayListWithExpectedSize(TOP_CHANNEL_COUNT);
    
    for (String channelName : channelNames) {
      stats.add(
          new ChannelStat(channelName, channelByteCounts.count(channelName)));
      if (stats.size() == TOP_CHANNEL_COUNT) {
        break;
      }
    }
    
    return stats;
  }
  
  public int getTotalByteCount() {
    return channelByteCounts.size();
  }

  public static void increment(JID fromJID, String msg, List<JID> toJIDs) {
    if (CACHE == null) {
      return;
    }
    
    ChannelStats stats = (ChannelStats) CACHE.get(STATS_CACHE_KEY);
    
    if (stats == null) {
      logger.warning("Initializing new ChannelStats");
      stats = new ChannelStats();
    }
    
    stats.incrementImpl(fromJID, msg, toJIDs);
    
    CACHE.put(STATS_CACHE_KEY, stats);
  }
  
  public static ChannelStats getCurrentStats() {
    if (CACHE == null) {
      return null;
    }
    
    return (ChannelStats) CACHE.get(STATS_CACHE_KEY);    
  }

}