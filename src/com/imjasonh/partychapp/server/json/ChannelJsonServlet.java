package com.imjasonh.partychapp.server.json;

import com.google.appengine.repackaged.com.google.common.collect.Lists;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.ppb.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ChannelJsonServlet  extends JsonServlet {
	private static final long serialVersionUID = 6640879543547767683L;
	@Override
	protected JSONObject getJson(HttpServletRequest req, HttpServletResponse resp,
			com.imjasonh.partychapp.User user, Datastore datastore) throws JSONException {
		String[] paths = req.getRequestURI().split("/");
		if (paths.length < 2) {
			return new JSONObject().put("error", "bad path");
		}
		String channelName = paths[paths.length - 1];

		Channel channel = datastore.getChannelIfUserPresent(channelName, user.getEmail());
		if (channel == null) {
			return new JSONObject().put("error", "access denied");
		}

		JSONObject returnObject = new JSONObject();
		JSONArray memberList = new JSONArray();
		List<Member> members = Lists.newArrayList(channel.getMembers());
		Collections.sort(members, new Member.SortMembersForListComparator());
		for (Member m : members) {
			JSONObject memberJson = new JSONObject();
			memberJson.put("alias", m.getAlias());
			memberJson.put("email", m.getEmail());
			memberList.put(memberJson);
		}
		returnObject.put("members", memberList);

		JSONArray targetList = new JSONArray();
		List<Target> targets = datastore.getTargetsByChannel(channel);
		for (Target t : targets) {
			JSONObject targetJson = new JSONObject();
			targetJson.put("name", t.name());
			targetJson.put("score", t.score());
			targetList.put(targetJson);
		}
		returnObject.put("targets", targetList);

		return returnObject;
	}
}
