package com.imjasonh.partychapp;

import com.google.appengine.api.xmpp.JID;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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
 * In-memory (via memcached) stats about channels. Not persisted in any way.
 *
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class ChannelStats implements Serializable {
  public class ChannelStat implements Serializable {
    private final String channelName;
    private int byteCount = 0;
    private int memberCount = 0;
    
    private ChannelStat(String channelName) {
      this.channelName = channelName;
    }
    
    public String getChannelName() {
      return channelName;
    }
    
    public int getByteCount() {
      return byteCount;
    }
    
    public int getMemberCount() {
      return memberCount;
    }
    
    private void incrementByteCount(int incBy) {
      byteCount += incBy;
    }
    
    private void setMemberCount(int memberCount) {
      this.memberCount = memberCount;
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
  
  private static final String STATS_CACHE_KEY = "channel-stats2";

  private static final int TOP_CHANNEL_COUNT = 50;  
  
  private final Date creationDate = new Date();
  private Date lastUpdateDate = new Date();
  private int totalByteCount = 0;
  private final Map<String, ChannelStat> channelStats = Maps.newHashMap();
  
  /**
   * Only {@link #increment} should be creating instances.
   */
  private ChannelStats() {
    // No further initialization needed
  }
  
  private void incrementImpl(JID fromJID, String msg, List<JID> toJIDs) {
    int byteCount = 0;
    
    byteCount += (fromJID.toString().length() + msg.length()) * toJIDs.size();
    for (JID toJID : toJIDs) {
      byteCount += toJID.toString().length();
    }
    
    String channelName = fromJID.getId().split("@")[0];
    ChannelStat stat = channelStats.get(channelName);
    if (stat == null) {
      stat = new ChannelStat(channelName);
      channelStats.put(channelName, stat);
    }
    
    stat.incrementByteCount(byteCount);
    // Approximate member count by storing the largest number of recipients seen
    // (we don't have the Channel object on hand, and we don't want to always
    // use recipients, since some messsages are sent just to a single member)
    if (toJIDs.size() > stat.getMemberCount()) {
      stat.setMemberCount(toJIDs.size());
    }
    
    lastUpdateDate = new Date();
    totalByteCount += byteCount;
  }
  
  public Date getCreationDate() {
    return creationDate;
  }
  
  public Date getLastUpdateDate() {
    return lastUpdateDate;
  }
  
  public List<ChannelStat> getTopChannels() {
    // TODO(mihaip): can be more efficient by using a heap
    List<String> channelNames = Lists.newArrayList(channelStats.keySet());
    Collections.sort(channelNames, new Comparator<String>() {
      @Override public int compare(String channelName1, String channelName2) {
        ChannelStat stat1 = channelStats.get(channelName1);
        ChannelStat stat2 = channelStats.get(channelName2);
        
        return Integer.valueOf(stat2.getByteCount())
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
  
  public int getTotalByteCount() {
    return totalByteCount;
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
