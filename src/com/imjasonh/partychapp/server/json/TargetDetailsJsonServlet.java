package com.imjasonh.partychapp.server.json;

import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.User;
import com.imjasonh.partychapp.ppb.Graphs;
import com.imjasonh.partychapp.ppb.Reason;
import com.imjasonh.partychapp.ppb.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TargetDetailsJsonServlet extends JsonServlet {
  private static final long serialVersionUID = 6640879543547767683L;

  @Override
  protected JSONObject getJson(
      HttpServletRequest req,
      HttpServletResponse resp,
      User user,
      Datastore datastore)
    throws JSONException {
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

    JSONArray reasonsJson = new JSONArray();
    Target t = datastore.getTarget(channel, targetName);
    for (Reason r : datastore.getReasons(t, 100)) {
      JSONObject reasonJson = new JSONObject();
      reasonJson.put("action", r.action());
      reasonJson.put("reason", r.reason());
      reasonJson.put("sender", r.sender().getAlias());
      reasonJson.put("timestampMsec", r.timestamp().getTime());
      reasonsJson.put(reasonJson);
    }

    JSONObject detailsJson = new JSONObject();
    detailsJson.put("reasons", reasonsJson);
    detailsJson.put(
        "graph",
        Graphs.getScoreGraph(Collections.singletonList(t), 400, 300));
    return detailsJson;
  }
}
