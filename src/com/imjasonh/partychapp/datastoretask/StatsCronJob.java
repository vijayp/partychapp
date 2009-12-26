package com.imjasonh.partychapp.datastoretask;

import com.imjasonh.partychapp.Configuration;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.WebRequest;
import com.imjasonh.partychapp.server.MailUtil;

public class StatsCronJob extends DatastoreTask {
  public void handle(WebRequest req, TestableQueue q) {
    Datastore.Stats stats = Datastore.instance().getStats();
    MailUtil.sendMail("pchat stats update",
                      stats.toString(),
                      "statscronjob@" + Configuration.mailDomain,
                      "nsanch@gmail.com");
  }
}
