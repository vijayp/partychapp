package com.imjasonh.partychapp.server.json;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.appengine.api.users.User;
import com.google.appengine.api.xmpp.JID;
import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.ppb.Reason;
import com.imjasonh.partychapp.ppb.Target;

public class UserInfoJsonServlet  extends JsonServlet {
	private static final long serialVersionUID = 6640879543547767683L;
	@SuppressWarnings("unused")
	
	@Override
	protected JSONObject getJson(HttpServletRequest req, HttpServletResponse resp,
			com.imjasonh.partychapp.User user, Datastore datastore) throws JSONException, IOException {
		JSONArray list = new JSONArray();
		for (String channelName: user.channelNames()) {
			JSONObject channelJson = new JSONObject();
			channelJson.put("name", channelName);
			Channel channel = datastore.getChannelByName(channelName);
			channelJson.put("alias", channel.getMemberByJID(new JID(user.getJID())).getAlias());
			list.put(channelJson);
		}

		return new JSONObject().put("channels", list);
	}
}
