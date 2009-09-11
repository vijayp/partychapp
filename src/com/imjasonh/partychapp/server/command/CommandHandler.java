package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;

public interface CommandHandler {
  public void doCommand(Message msg);
  
  public boolean matches(Message msg);
  
  public String documentation();
}