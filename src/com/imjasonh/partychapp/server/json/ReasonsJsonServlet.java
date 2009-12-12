package com.imjasonh.partychapp.server.json;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.appengine.api.users.User;
import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.ppb.Reason;
import com.imjasonh.partychapp.ppb.Target;

public class ReasonsJsonServlet  extends JsonServlet {
	private static final long serialVersionUID = 6640879543547767683L;
	@SuppressWarnings("unused")
	
	@Override
	protected JSONObject getJson(HttpServletRequest req, HttpServletResponse resp,
			com.imjasonh.partychapp.User user, Datastore datastore) throws JSONException, IOException {
		String[] paths = req.getRequestURI().split("/");
		if (paths.length < 3) {
			return new JSONObject().put("error", "bad path");
		}
		String channelName = paths[paths.length - 2];
		String targetName = paths[paths.length - 1];

		Channel channel = datastore.getChannelIfUserPresent(channelName, user.getEmail());
		if (channel == null) {
			return new JSONObject().put("error", "access denied");
		}

		JSONArray list = new JSONArray();
		Target t = datastore.getTarget(channel, targetName);
		for (Reason r: datastore.getReasons(t, 100)) {
			JSONObject reasonJson = new JSONObject();
			reasonJson.put("reason", r.reason());
			reasonJson.put("sender", r.sender().getAlias());
			list.put(reasonJson);
		}

		return new JSONObject().put("reasons", list);
	}
}
