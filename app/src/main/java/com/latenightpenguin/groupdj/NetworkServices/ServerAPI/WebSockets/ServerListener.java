package com.latenightpenguin.groupdj.NetworkServices.ServerAPI.WebSockets;

import android.util.Log;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class ServerListener extends WebSocketListener {
    private static final int NORMAL_CLOSURE_STATUS = 1000;
    private MessageHandler messageHandler;
    private MessageHandler errorHandler;

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        super.onOpen(webSocket, response);
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        super.onMessage(webSocket, text);

        if(messageHandler != null) {
            messageHandler.handle(text);
        }
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        webSocket.close(NORMAL_CLOSURE_STATUS, null);
        errorHandler.handle("closing");
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        t.printStackTrace();
        String exception = t.getMessage();
        Log.d("WebSocket", "Error connecting to server " + response);
        errorHandler.handle("failed");
        //throw new Exception("Can't connect to server");
    }

    public void setMessageHandler(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    public void setErrorHandler(MessageHandler errorHandler) {
        this.errorHandler = errorHandler;;
    }

    public interface MessageHandler {
        void handle(String message);
    }
}
