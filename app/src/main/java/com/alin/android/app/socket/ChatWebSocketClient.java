package com.alin.android.app.socket;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class ChatWebSocketClient extends WebSocketClient {

    public ChatWebSocketClient(URI serverUri) {
        // Draft_6455()代表使用的协议版本
        super(serverUri, new Draft_6455());
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        Log.e("ChatWebSocketClient", "onOpen()");
    }

    @Override
    public void onMessage(String message) {
        Log.e("ChatWebSocketClient", "onMessage()");
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.e("ChatWebSocketClient", "onClose()");
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
        Log.e("ChatWebSocketClient", "onError()");
    }
}