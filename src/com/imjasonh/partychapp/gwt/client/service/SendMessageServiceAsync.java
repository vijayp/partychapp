package com.imjasonh.partychapp.gwt.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface SendMessageServiceAsync {

	void sendMessage(String message, AsyncCallback<Void> callback);
}
