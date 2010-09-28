package com.imjasonh.partychapp.gwt.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.imjasonh.partychapp.gwt.client.service.SendMessageService;

public class SendMessageImpl extends RemoteServiceServlet implements
		SendMessageService {

	private static final long serialVersionUID = 1L;

	@Override
	public void sendMessage(String message) {

	}
}
