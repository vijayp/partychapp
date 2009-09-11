package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;

public enum Command {
	LEAVE(new LeaveHandler()),
	LIST(new ListHandler()),
	HELP(new HelpHandler()),
	ALIAS(new AliasHandler()),
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
	  return null;
  }
}
