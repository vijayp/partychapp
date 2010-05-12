package com.imjasonh.partychapp.server.json;

import com.google.appengine.api.xmpp.JID;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserInfoJsonServlet extends JsonServlet {
  private static final Logger logger =
      Logger.getLogger(UserInfoJsonServlet.class.getName());
  
  private static final long serialVersionUID = 6640873547765357683L;

  static public JSONObject getJsonFromUser(User user,
      Datastore datastore) throws JSONException {
    JSONArray list = new JSONArray();
    for (Channel channel : user.getChannels()) {
      Member member = channel.getMemberByJID(new JID(user.getJID()));
      if (member == null) {
        logger.warning("Could not actually find member " + 
            user.getJID() + " in channel " + channel.getName());
        continue;
      }
      
      JSONObject channelJson = new JSONObject();
      channelJson.put("name", channel.getName());
      channelJson.put("alias", member.getAlias());
      list.put(channelJson);
    }

    JSONObject jsonResponse = new JSONObject().put("channels", list);
    jsonResponse.put("email", user.getEmail());
    return jsonResponse;
  }

  @Override
  protected JSONObject getJson(
      HttpServletRequest req,
      HttpServletResponse resp, User user,
      Datastore datastore)
      throws JSONException {
    return getJsonFromUser(user, datastore);
  }
}
