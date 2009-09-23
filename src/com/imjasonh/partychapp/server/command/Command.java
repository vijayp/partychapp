package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;

public enum Command {
  // these have to be first
  CREATE_AND_JOIN(new CreateAndJoinCommand()),
  JOIN(new JoinCommand()),
  
  // these can be in any order
  LEAVE(new LeaveHandler()),
  LIST(new ListHandler()),
  HELP(new HelpHandler()),
  ALIAS(new AliasHandler()),
  SCORE(new ScoreHandler()),
  REASONS(new ReasonsHandler()),
  PLUSPLUSBOT(new PPBHandler()),
  ME(new MeHandler()),
  INVITE(new InviteHandler()),
  INVITE_ONLY(new InviteOnlyHandler()),
  KICK(new KickHandler()),
  STATUS(new StatusHandler()),
  SUMMON(new SummonHandler()),
  
  // this has to be after the slash-commands
  SEARCHREPLACE(new SearchReplaceHandler()),

  // this has to be last
  BROADCAST(new BroadcastHandler()),
  ;

  public final CommandHandler commandHandler;

  private Command(CommandHandler commandHandler) {
    this.commandHandler = commandHandler;
  }

  public static CommandHandler getCommandHandler(Message msg) {
    for (Command command : Command.values()) {
      if (command.commandHandler.matches(msg)) {
        return command.commandHandler;
      }
    }
    throw new RuntimeException("getCommandHandler should never return null, " +
                               "but we can't find a match. msg = " + msg.toString());
  }
}
