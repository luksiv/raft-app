package com.latenightpenguin.groupdj.NetworkServices.ServerAPI.WebSockets;

import com.latenightpenguin.groupdj.NetworkServices.ServerAPI.WebSocketStatus;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;

public class WebSocketHelper {
    private String serverURL;
    private OkHttpClient client;
    private WebSocket websocket;
    private WebSocketStatus status;

    private IWebSocketCallback playingNextCallback;
    private IWebSocketCallback songAddedCallback;
    private IWebSocketCallback songPausedCallback;
    private IWebSocketCallback songPlayTimeCallback;
    private IWebSocketCallback songSkippedCallback;
    private IWebSocketCallback connectedToRoomCallback;

    public WebSocketHelper(String url){
        serverURL = url;
    }

    public void connectWebSocket() {
        Request request = new Request.Builder().url(serverURL).build();
        ServerListener serverListener = new ServerListener();
        serverListener.setMessageHandler(messageHandler);
        serverListener.setErrorHandler(errorHandler);
        websocket = client.newWebSocket(request, serverListener);
        status = WebSocketStatus.CONNECTED;
    }

    public void setRoomUpdates(int id) {
        if (status == WebSocketStatus.CONNECTED) {
            websocket.send("room;" + id);
        }
    }

    public void announcePlayTime(long milliseconds) {
        if (status == WebSocketStatus.CONNECTED) {
            websocket.send("play;" + milliseconds);
        }
    }

    public void announcePause(long milliseconds) {
        if (status == WebSocketStatus.CONNECTED) {
            websocket.send("pause;" + milliseconds);
        }
    }

    public void closeWebSocket() {
        if (status == WebSocketStatus.CONNECTED) {
            websocket.close(1000, "");
        }
    }

    public void setPlayingNextCallback(IWebSocketCallback playingNextCallback) {
        this.playingNextCallback = playingNextCallback;
    }

    public void setSongAddedCallback(IWebSocketCallback songAddedCallback) {
        this.songAddedCallback = songAddedCallback;
    }

    public void setSongPausedCallback(IWebSocketCallback songPausedCallback) {
        this.songPausedCallback = songPausedCallback;
    }

    public void setSongPlayTimeCallback(IWebSocketCallback songPlayTimeCallback) {
        this.songPlayTimeCallback = songPlayTimeCallback;
    }

    public void setSongSkippedCallback(IWebSocketCallback songSkipedCallback) {
        this.songSkippedCallback = songSkipedCallback;
    }

    public void setConnectedToRoomCallback(IWebSocketCallback connectedToRoomCallback) {
        this.connectedToRoomCallback = connectedToRoomCallback;
    }

    public WebSocketStatus getStatus() {
        return status;
    }

    private ServerListener.MessageHandler messageHandler = new ServerListener.MessageHandler() {
        @Override
        public void handle(String message) {
            switch (message) {
                case "song added":
                    if (songAddedCallback != null) {
                        songAddedCallback.execute("");
                    }
                    break;
                case "next song":
                    if (playingNextCallback != null) {
                        playingNextCallback.execute("");
                    }
                    break;
                case "skip":
                    if(songSkippedCallback != null){
                        songSkippedCallback.execute("");
                    }
                    break;
                default:
                    if(message.startsWith("play")){
                        String[] fields = message.split(":");
                        if (songPlayTimeCallback != null) {
                            songPlayTimeCallback.execute(fields[1]);
                        }
                    } else if(message.startsWith("paused")){
                        String[] fields = message.split(":");
                        if (songPausedCallback != null) {
                            songPausedCallback.execute(fields[1]);
                        }
                    } else {
                        if(connectedToRoomCallback != null){
                            connectedToRoomCallback.execute(message);
                        }
                    }
                    break;
            }
        }
    };

    private ServerListener.MessageHandler errorHandler = new ServerListener.MessageHandler() {
        @Override
        public void handle(String message) {
            status = WebSocketStatus.DISCONNECTED;
            if (message.equals("failed")) {
//                connectWebSocket();
            }
        }
    };
}
