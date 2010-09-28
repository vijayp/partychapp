package com.imjasonh.partychapp.gwt.client.service;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("send")
public interface SendMessageService extends RemoteService {

	public void sendMessage(String message);
}
