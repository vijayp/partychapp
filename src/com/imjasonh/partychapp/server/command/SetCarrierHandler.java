package com.imjasonh.partychapp.server.command;

import java.util.Map;

import com.google.appengine.repackaged.com.google.common.collect.Maps;
import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.Message;

public class SetCarrierHandler extends SlashCommand {

  public static Map<String, Member.Carrier> supportedCarriers;
  static {
    supportedCarriers = Maps.newHashMap();
    for (Member.Carrier c : Member.Carrier.values()) {
      supportedCarriers.put(c.shortName, c);
    }
  }
  
  public SetCarrierHandler() {
    super("set-carrier");
  }

  @Override
  void doCommand(Message msg, String argument) {
    Member.Carrier carrier = null;
    if (argument != null) {
      argument = argument.trim().toLowerCase();
      carrier = supportedCarriers.get(argument);
    }
    if (carrier == null) {
      String supported = "";
      for (Member.Carrier c : Member.Carrier.values()) {
        supported += c.shortName + " ";
      }
      msg.channel.sendDirect("Unsupported carrier " + argument +
                             ". Supported carriers are: " + supported,
                             msg.member);
      return;
    }
    
    msg.member.setCarrier(carrier);
    msg.member.put();
    
    msg.channel.sendDirect("okay, set your carrier to " + carrier.shortName,
                           msg.member);
  }

  public String documentation() {
    return null; 
  }

}
