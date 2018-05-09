package com.latenightpenguin.groupdj.NetworkServices.ServerAPI.WebSockets;

import android.util.Log;

import com.latenightpenguin.groupdj.NetworkServices.ServerAPI.WebSocketStatus;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;

public class WebSocketHelper {
    private static final String TAG = "WebSocketHelper";
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
        client = new OkHttpClient();
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
            Log.d(TAG, "Recieved message from websocket");
            Log.d(TAG, message);
            switch (message) {
                case "song added":
                    if (songAddedCallback != null) {
                        Log.d(TAG, "calling songAddedCallback");
                        songAddedCallback.execute("");
                    } else {
                        Log.d(TAG, "songAddedCallback is null");
                    }
                    break;
                case "next song":
                    if (playingNextCallback != null) {
                        Log.d(TAG, "calling playingNextCallback");
                        playingNextCallback.execute("");
                    } else {
                        Log.d(TAG, "playingNextCallback is null");
                    }
                    break;
                case "skip":
                    if(songSkippedCallback != null){
                        Log.d(TAG, "calling songSkippedCallback");
                        songSkippedCallback.execute("");
                    } else {
                        Log.d(TAG, "songSkippedCallback is null");
                    }
                    break;
                default:
                    if(message.startsWith("play")){
                        String[] fields = message.split(":");
                        if (songPlayTimeCallback != null) {
                            Log.d(TAG, "calling songPlayTimeCallback " + fields[1] );
                            songPlayTimeCallback.execute(fields[1]);
                        } else {
                            Log.d(TAG, "songPlayTimeCallback is null");
                        }
                    } else if(message.startsWith("paused")){
                        String[] fields = message.split(":");
                        if (songPausedCallback != null) {
                            Log.d(TAG, "calling songPausedCallback");
                            songPausedCallback.execute(fields[1]);
                        } else {
                            Log.d(TAG, "songPausedCallback is null");
                        }
                    } else {
                        if(connectedToRoomCallback != null){
                            Log.d(TAG, "calling connectedToRoomCallback");
                            connectedToRoomCallback.execute(message);
                        } else {
                            Log.d(TAG, "connectedToRoomCallback is null");
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
