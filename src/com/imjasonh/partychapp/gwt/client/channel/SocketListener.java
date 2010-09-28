package com.imjasonh.partychapp.gwt.client.channel;

public interface SocketListener {
    void onOpen();
    void onMessage(String message);
}
