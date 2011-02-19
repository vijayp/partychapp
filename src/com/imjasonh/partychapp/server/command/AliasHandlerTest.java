package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;

public class AliasHandlerTest extends CommandHandlerTestCase {
  AliasHandler handler = new AliasHandler();

  public void testAliasMatches() {
    assertTrue(handler.matches(Message.createForTests("/alias nsanch")));
    assertTrue(handler.matches(Message.createForTests(" /alias with-a-space-in-front")));
    assertFalse(handler.matches(Message.createForTests("no you shouldn't handle this string with /alias")));
    assertTrue(handler.matches(Message.createForTests("/alias abcABC123-_*'")));
  }
  
  public void testAllowedAliases() {
    assertTrue(isAliasAllowed("nsanch"));
    assertTrue(isAliasAllowed("nsanch2000"));
    assertTrue(isAliasAllowed("nsanch-2000"));
    assertTrue(isAliasAllowed("\u30DF\u30CF\u30A4")); // Katakana mi-ha-i 
    assertTrue(isAliasAllowed("\u2603")); // Snowman
    assertFalse(isAliasAllowed("nsa;nch;"));
    assertFalse(isAliasAllowed("<script>alert('xss');</script>"));
    assertTrue(isAliasAllowed("mihai.parparita"));
}
  
  private boolean isAliasAllowed(String alias) {
    xmpp.messages.clear();
    handler.doCommand(Message.createForTests("/alias " + alias));
    return !xmpp.messages.get(0).getBody().contains("That alias contains invalid characters");
  }
}
