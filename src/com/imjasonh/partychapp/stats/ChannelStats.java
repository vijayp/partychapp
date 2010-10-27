package com.imjasonh.partychapp.stats;

import com.google.appengine.api.memcache.jsr107cache.GCacheFactory;
import com.google.appengine.api.xmpp.JID;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.imjasonh.partychapp.Configuration;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheManager;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


/**
 * In-memory (via memcached) stats about channels. Not persisted or sychronized 
 * in any way, stats may go away at any point and concurrent requests may result
 * in data loss.
 * Usage:
 * 1. Install ChannelStatsFilter as a servlet filter in front of all servlets
 * 2. Call ChannelStats.record* static methods to record per-channel statistics
 * 3. Call ChannelStats.getCurrentStats() for current statistics
 *
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class ChannelStats implements Serializable {
  public static class ChannelStat implements Serializable {
    private final String channelName;
    private long byteCount = 0;
    private long memberCount = 0;
    private long messagePreFanoutCount = 0;
    private long messagePostFanoutCount = 0;
    private long cpuMegaCycles = 0;
    
    private ChannelStat(String channelName) {
      this.channelName = channelName;
    }
    
    public String getChannelName() {
      return channelName;
    }
    
    public long getByteCount() {
      return byteCount;
    }
    
    public long getMemberCount() {
      return memberCount;
    }
    
    public long getMessagePreFanoutCount() {
      return messagePreFanoutCount;
    }
    
    public long getMessagePostFanoutCount() {
      return messagePostFanoutCount;
    }
    
    public long getCpuMegaCycles() {
      return cpuMegaCycles;
    }

    private void incrementByteCount(long incBy) {
      byteCount += incBy;
    }
    
    private void setMemberCount(long memberCount) {
      // Approximate member count by storing the largest number of recipients seen
      // (we don't have the Channel object on hand, and we don't want to always
      // use recipients, since some messsages are sent just to a single member)
      if (memberCount > this.memberCount) {
        this.memberCount = memberCount;
      }
    }
    
    private void incrementMessageCount(long recipientCount) {
      if (recipientCount > 0) {
        messagePreFanoutCount++;
        messagePostFanoutCount += recipientCount;
      }
    }
    
    private void incrementCpuMegaCycles(long cpuMegaCycles) {
      this.cpuMegaCycles += cpuMegaCycles;
    }
    
    private void merge(ChannelStat src) {
      Preconditions.checkArgument(src.channelName.equals(channelName));
      incrementByteCount(src.byteCount);
      setMemberCount(src.memberCount);
      incrementMessageCount(src.messagePostFanoutCount);
      incrementCpuMegaCycles(src.cpuMegaCycles);
    }
  }
  
  private static final Logger logger =
      Logger.getLogger(ChannelStats.class.getName());
  
  static final ThreadLocal<List<ChannelStat>> perRequestStats = 
      new ThreadLocal<List<ChannelStat>>() {
        @Override protected List<ChannelStat> initialValue() {
          return Lists.newArrayList();
        }
      };  
  
  private static final long STATS_EXPIRATION_SEC = 24 * 60 * 60L;
  private static final long STATS_EXPIRATION_MSEC = STATS_EXPIRATION_SEC * 1000;
  
  private static Cache cache = null;
  static {
    try {
      cache = CacheManager.getInstance().getCacheFactory().createCache(
          ImmutableMap.of(
              GCacheFactory.EXPIRATION_DELTA, STATS_EXPIRATION_SEC));
    } catch (CacheException err) {
      logger.warning("Could not initialize ChannelStats cache");
    }
  }
  
  private static final String STATS_CACHE_KEY = "channel-stats5";

  private static final int TOP_CHANNEL_COUNT = 50;  
  
  private final Date creationDate = new Date();
  private Date lastUpdateDate = new Date();
  private long totalByteCount = 0;
  private long totalMessagePreFanoutCount = 0;
  private long totalMessagePostFanoutCount = 0;
  private long totalCpuMegaCycles = 0;
  private final Map<String, ChannelStat> channelStats = Maps.newHashMap();
  
  /**
   * Only {@link #recordStats} should be creating instances.
   */
  ChannelStats() {
    // No further initialization needed
  }
  
  public static void recordStats(List<ChannelStat> requestStats) {
    if (cache == null || !areChannelStatsEnabled()) {
      return;
    }
    
    ChannelStats stats = (ChannelStats) cache.get(STATS_CACHE_KEY);
    
    if (stats == null || stats.areTooOld()) {
      logger.warning("Initializing new ChannelStats");
      stats = new ChannelStats();
    }
    
    for (ChannelStat stat : requestStats) {
      stats.recordStat(stat);
    }
    
    stats.lastUpdateDate = new Date();
    
    cache.put(STATS_CACHE_KEY, stats);
  }
  
  private void recordStat(ChannelStat stat) {
    ChannelStat existingStat = channelStats.get(stat.getChannelName());
    if (existingStat == null) {
      channelStats.put(stat.getChannelName(), stat);      
    } else {
      existingStat.merge(stat);
    }
    
    totalByteCount += stat.getByteCount();
    totalMessagePreFanoutCount += stat.getMessagePreFanoutCount();
    totalMessagePostFanoutCount += stat.getMessagePostFanoutCount();
    totalCpuMegaCycles += stat.getCpuMegaCycles();
  }
  
  public Date getCreationDate() {
    return creationDate;
  }
  
  public boolean areTooOld() {
    return new Date().getTime() - creationDate.getTime() > STATS_EXPIRATION_MSEC;
  }
  
  public Date getLastUpdateDate() {
    return lastUpdateDate;
  }
  
  public List<ChannelStat> getTopChannels() {
    if (!areChannelStatsEnabled()) {
      return Collections.emptyList();
    }
    // TODO(mihaip): can be more efficient by using a heap
    List<String> channelNames = Lists.newArrayList(channelStats.keySet());
    Collections.sort(channelNames, new Comparator<String>() {
      @Override public int compare(String channelName1, String channelName2) {
        ChannelStat stat1 = channelStats.get(channelName1);
        ChannelStat stat2 = channelStats.get(channelName2);
        
        return Long.valueOf(stat2.getByteCount())
            .compareTo(stat1.getByteCount());
      }
    });
    
    List<ChannelStat> topChannelStats =
          Lists.newArrayListWithExpectedSize(TOP_CHANNEL_COUNT);
    
    for (String channelName : channelNames) {
      topChannelStats.add(channelStats.get(channelName));
      if (topChannelStats.size() == TOP_CHANNEL_COUNT) {
        break;
      }
    }
    
    return topChannelStats;
  }
  
  public long getTotalByteCount() {
    return totalByteCount;
  }
  
  public long getTotalMessagePreFanoutCount() {
    return totalMessagePreFanoutCount;
  }

  public long getTotalMessagePostFanoutCount() {
    return totalMessagePostFanoutCount;
  }
  
  public long getTotalCpuMegaCycles() {
    return totalCpuMegaCycles;
  }

  public static void recordMessageSend(
      JID fromJID, String msg, List<JID> toJIDs) {
    if (!areChannelStatsEnabled()) return;
    String channelName = fromJID.getId().split("@")[0];
    ChannelStat stat = new ChannelStat(channelName);
        
    long byteCount = 0;
    
    byteCount += (fromJID.toString().length() + msg.length()) * toJIDs.size();
    for (JID toJID : toJIDs) {
      byteCount += toJID.toString().length();
    }
    
    stat.incrementByteCount(byteCount);
    stat.setMemberCount(toJIDs.size());
    stat.incrementMessageCount(toJIDs.size());
    
    perRequestStats.get().add(stat);
  }
  
  public static void recordChannelCpu(String channelName, long cpuMegaCycles) {
    if (!areChannelStatsEnabled()) return;
    ChannelStat stat = new ChannelStat(channelName);
    stat.incrementCpuMegaCycles(cpuMegaCycles);
    
    perRequestStats.get().add(stat);
  }
  
  
  public static ChannelStats getCurrentStats() {
    if (cache == null || !areChannelStatsEnabled()) {
      return null;
    }
    
    return (ChannelStats) cache.get(STATS_CACHE_KEY);    
  }

  public static void reset() {
    if (cache != null || !areChannelStatsEnabled()) {
      cache.remove(STATS_CACHE_KEY);
    }
  }
  
  private static boolean areChannelStatsEnabled() {
    return Configuration.persistentConfig().areChannelStatsEnabled(); 
  }
}
