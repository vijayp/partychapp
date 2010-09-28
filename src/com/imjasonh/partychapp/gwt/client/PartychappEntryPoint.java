package com.imjasonh.partychapp.gwt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.imjasonh.partychapp.gwt.client.channel.Channel;
import com.imjasonh.partychapp.gwt.client.channel.ChannelFactory;
import com.imjasonh.partychapp.gwt.client.channel.SocketListener;
import com.imjasonh.partychapp.gwt.client.service.SendMessageServiceAsync;
import com.imjasonh.partychapp.gwt.client.service.SendMessageService;

public class PartychappEntryPoint implements EntryPoint {
	@Override
	public void onModuleLoad() {
		String channelName = Dictionary.getDictionary("info").get("channel");

		Channel channel = ChannelFactory.createChannel(channelName);
		channel.open(new SocketListener() {
			@Override
			public void onOpen() {
				RootPanel.get().add(new Label("connected!"));
			}

			@Override
			public void onMessage(String message) {
				RootPanel.get().add(new Label(message));
			}
		});

		final SendMessageServiceAsync service = GWT.create(SendMessageService.class);
		final TextBox textBox = new TextBox();

		final AsyncCallback<Void> callback = new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				Window.alert(caught.getMessage());
			}
			@Override
			public void onSuccess(Void result) {
				textBox.setText("");
				Window.alert("Sent!");
			}
		};
		textBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				service.sendMessage(textBox.getText(), callback);
			}
		});
		RootPanel.get().add(textBox);
	}
}
