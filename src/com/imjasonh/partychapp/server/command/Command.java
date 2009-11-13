package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;

public enum Command {
  // just to avoid craziness, let's assume we only let people broadcast from
  // email and sms, so let's steal these and never let the slash-commands see
  // them.
  EMAIL(new EmailHandler()),
  SMS(new SMSHandler()),
  
  // these implicit handlers have to be first
  CREATE_AND_JOIN(new CreateAndJoinCommand()),
  JOIN(new JoinCommand()),
  
  // these can be in any order
  LEAVE(new LeaveHandler()),
  LIST(new ListHandler()),
  HELP(new HelpHandler()),
  ALIAS(new AliasHandler()),
  SCORE(new ScoreHandler()),
  REASONS(new ReasonsHandler()),
  ME(new MeHandler()),
  INVITE(new InviteHandler()),
  INVITE_ONLY(new InviteOnlyHandler()),
  KICK(new KickHandler()),
  STATUS(new StatusHandler()),
  SUMMON(new SummonHandler()),
  UNDO(new UndoHandler()),
  DEBUG(new DebugHandler()),
  STATS(new StatsHandler()),
  GRAPH_SCORES(new GraphScoreHandler()),
  SNOOZE(new SnoozeHandler()),

  // these have to be after the slash-commands
  SEARCHREPLACE(new SearchReplaceHandler()),
  PLUSPLUSBOT(new PPBHandler()),

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
