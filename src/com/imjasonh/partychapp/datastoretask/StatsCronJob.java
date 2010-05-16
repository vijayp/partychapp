package com.imjasonh.partychapp.datastoretask;

import java.net.URL;
import java.util.Date;
import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.imjasonh.partychapp.Configuration;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.PersistentConfiguration;
import com.imjasonh.partychapp.WebRequest;
import com.imjasonh.partychapp.server.MailUtil;

public class StatsCronJob extends DatastoreTask {
  private static final Logger logger =
    Logger.getLogger(StatsCronJob.class.getName());
  
  @Override
  public void handle(WebRequest req, TestableQueue q) {
    Datastore.Stats stats = Datastore.instance().getStats(false);
    MailUtil.sendMail("pchat stats update",
                      stats.toString(),
                      "statscronjob@" + Configuration.mailDomain,
                      Configuration.statsEmailAddress);

    PersistentConfiguration pc = Configuration.persistentConfig();
    if (pc == null ||
        Strings.isNullOrEmpty(pc.sessionToken()) ||
        Strings.isNullOrEmpty(pc.listFeedUrl())) {
      return;
    }
    
    SpreadsheetService service = new SpreadsheetService("partychapp");
    String sessionToken = Configuration.persistentConfig().sessionToken();
    service.setAuthSubToken(sessionToken, null);
    ListEntry newEntry = new ListEntry();   
    newEntry.getCustomElements().setValueLocal("date",
                                               (new Date()).toString());
    newEntry.getCustomElements().setValueLocal("channels", Integer.toString(stats.numChannels));
    newEntry.getCustomElements().setValueLocal("onedays",
                                               Integer.toString(stats.oneDayActiveUsers));
    newEntry.getCustomElements().setValueLocal("sevendays",
                                               Integer.toString(stats.sevenDayActiveUsers));
    newEntry.getCustomElements().setValueLocal("thirtydays",
                                               Integer.toString(stats.thirtyDayActiveUsers));
    newEntry.getCustomElements().setValueLocal("Users",
                                               Integer.toString(stats.numUsers));
    newEntry.getCustomElements().setValueLocal("notes",
                                               "");
    String listFeedUrl = Configuration.persistentConfig().listFeedUrl();
    try {
      service.insert(new URL(listFeedUrl), newEntry);
    } catch (Exception e) {
      logger.warning("Failure writing stats back to docs: " + e);
    }
  }
}
