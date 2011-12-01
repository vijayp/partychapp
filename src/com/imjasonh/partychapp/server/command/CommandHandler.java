package com.imjasonh.partychapp.server.command;

import javax.servlet.http.HttpServletResponse;

import com.imjasonh.partychapp.Message;

public interface CommandHandler {
  public void doCommand(Message msg, HttpServletResponse resp);
  public void doCommand(Message msg);

  
  public boolean matches(Message msg);
  
  public String documentation();

}