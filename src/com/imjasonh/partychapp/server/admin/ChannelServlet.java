package com.imjasonh.partychapp.server.admin;

import com.google.appengine.repackaged.com.google.common.collect.Lists;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.User;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Dumps basic information about a channel.
 * 
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public class ChannelServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    if (Strings.isNullOrEmpty(req.getPathInfo())) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    
    // Strip leading slash to get channel name
    String channelName = req.getPathInfo().substring(1);
    
    Datastore datastore = Datastore.instance();
    datastore.startRequest();
    try {
      Channel channel = datastore.getChannelByName(channelName);
      
      if (channel == null) {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
      }
      
      resp.setContentType("text/plain; charset=utf-8");
      
      Writer writer = resp.getWriter();
      writer.write("Name: " + channel.getName() + "\n");
      writer.write("Invite only: " + channel.isInviteOnly() + "\n");
      writer.write("Invitees:\n");
      for (String invitee : channel.getInvitees()) {
        writer.write("\t" + invitee + "\n");
      }
      writer.write("Members:\n");
      
      List<Member> members = Lists.newArrayList(channel.getMembers());
      
      // TODO(mihaip): stop looking up the User for each Member once issue 65
      // is fixed.
      final Map<String, Date> memberJidToLastSeen = Maps.newHashMap();
      for (Member member : members) {
        User memberUser = Datastore.instance().getUserByJID(member.getJID());
        if (memberUser != null) {
          memberJidToLastSeen.put(member.getJID(), memberUser.lastSeen());
        }
      }
      
      Collections.sort(members, new Comparator<Member>() {
        @Override public int compare(Member member1, Member member2) {
          Date lastSeen1 = memberJidToLastSeen.get(member1.getJID());
          Date lastSeen2 = memberJidToLastSeen.get(member2.getJID());
          
          if (lastSeen1 == null) {
            return 1;
          }
          if (lastSeen2 == null) {
            return -1;
          }
          
          return lastSeen2.compareTo(lastSeen1);
       }});
      
      for (Member member : members) {
        writer.write("\t" + member.getJID() + "\n");
        writer.write("\t\tlast seen: ");
        if (memberJidToLastSeen.get(member.getJID()) != null) {
          writer.write(memberJidToLastSeen.get(member.getJID()).toString());
        } else {
          writer.write("unknown");
        }
        writer.write("\n");
        writer.write("\t\talias: " + member.getAlias() + "\n");
        writer.write("\t\tsnooze: " + member.getSnoozeStatus() + "\n");
        writer.write("\t\tlast messages :\n");
        for (String message : member.getLastMessages()) {
          writer.write("\t\t\t" + message + "\n");
        }
      }
    } finally {
      datastore.endRequest();
    }
  }
}
