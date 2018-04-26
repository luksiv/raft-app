package com.latenightpenguin.groupdj.NetworkServices.ServerAPI;

import android.util.Log;

import com.latenightpenguin.groupdj.NetworkServices.ServerAPI.Requests.ConnectionManager;
import com.latenightpenguin.groupdj.NetworkServices.ServerAPI.Requests.ServerRequest;
import com.latenightpenguin.groupdj.NetworkServices.ServerAPI.WebSockets.ServerListener;
import com.latenightpenguin.groupdj.RoomInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;

public class ServerHelper implements IServerHelper{
    public static String SERVER_URL;
    private static String SERVER_WEBSOCKET_URL;
    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_DELETE = "DELETE";
    public static final String CONNECTION_ERROR = "There was error connecting to the server";
    public static final String RESPONSE_ERROR = "There was error getting response";

    private OkHttpClient client;
    private WebSocket websocket;
    private WebSocketStatus status;
    private int roomId;
    private int activeRequests;

    // websocket callbacks
    private ICallback playingNextCallback;
    private ICallback songAddedCallback;
    private ICallback songPausedCallback;
    private ICallback songPlayTimeCallback;
    private ICallback songSkipedCallback;
    private ICallback connectedToRoomCallback;

    public ServerHelper(String url) {
        SERVER_URL = "http://" + url;
        SERVER_WEBSOCKET_URL = "ws://" + url;
        client = new OkHttpClient();
        roomId = -1;
        status = WebSocketStatus.DISCONNECTED;
        activeRequests = 0;
    }

    //region WebSockets
    @Override
    public void connectWebSocket() {
        Request request = new Request.Builder().url(SERVER_WEBSOCKET_URL).build();
        ServerListener serverListener = new ServerListener();
        serverListener.setMessageHandler(messageHandler);
        serverListener.setErrorHandler(errorHandler);
        websocket = client.newWebSocket(request, serverListener);
        status = WebSocketStatus.CONNECTED;
        if (roomId != -1) {
            setRoomUpdates(roomId);
        }
    }

    @Override
    public void setRoomUpdates(int id) {
        roomId = id;
        if (status == WebSocketStatus.CONNECTED) {
            websocket.send("room;" + id);
        }
    }

    @Override
    public void announcePlayTime(long milliseconds) {
        if (status == WebSocketStatus.CONNECTED) {
            websocket.send("play;" + milliseconds);
        }
    }

    @Override
    public void announcePause(long milliseconds) {
        if (status == WebSocketStatus.CONNECTED) {
            websocket.send("pause;" + milliseconds);
        }
    }

    @Override
    public void closeWebSocket() {
        if (status == WebSocketStatus.CONNECTED) {
            websocket.close(1000, "");
        }
    }

    @Override
    public void setPlayingNextCallback(ICallback playingNextCallback) {
        this.playingNextCallback = playingNextCallback;
    }

    @Override
    public void setSongAddedCallback(ICallback songAddedCallback) {
        this.songAddedCallback = songAddedCallback;
    }

    @Override
    public void setSongPausedCallback(ICallback songPausedCallback) {
        this.songPausedCallback = songPausedCallback;
    }

    @Override
    public void setSongPlayTimeCallback(ICallback songPlayTimeCallback) {
        this.songPlayTimeCallback = songPlayTimeCallback;
    }

    @Override
    public void setSongSkippedCallback(ICallback songSkipedCallback) {
        this.songSkipedCallback = songSkipedCallback;
    }

    @Override
    public void setConnectedToRoomCallback(ICallback connectedToRoomCallback) {
        this.connectedToRoomCallback = connectedToRoomCallback;
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
                    if(songSkipedCallback != null){
                        songSkipedCallback.execute("");
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
    //endregion

    @Override
    public WebSocketStatus getWebSocketStatus() {
        return status;
    }

    @Override
    public void registerUser(String user, final ICallback callback) {
        ICallback cb = new ICallback() {
            @Override
            public void execute(String response) {
                activeRequests--;
                callback.execute(response);
            }
        };
        ServerRequest request = new ServerRequest(METHOD_POST, "api/users", "\"" + user + "\"", cb, null);
        new ConnectionManager().execute(request);
        activeRequests++;
    }

    @Override
    public void createRoom(String user, final ICallback callback) {
        ICallback cb = new ICallback() {
            @Override
            public void execute(String response) {
                activeRequests--;
                callback.execute(response);
            }
        };
        ServerRequest request = new ServerRequest(METHOD_POST, "api/rooms", "\"" + user + "\"", cb, null);
        new ConnectionManager().execute(request);
        activeRequests++;
    }

    @Override
    public void connectToRoom(String user, int loginCode, final ICallback callback) {
        ICallback cb = new ICallback() {
            @Override
            public void execute(String response) {
                activeRequests--;
                callback.execute(response);
            }
        };
        ServerRequest request = new ServerRequest(METHOD_POST, "api/rooms", "\"" + user + "\"", cb, null);
        new ConnectionManager().execute(request);
        activeRequests++;
    }

    @Override
    public void addSong(RoomInfo room, String song, final ICallback callback) {
        ICallback cb = new ICallback() {
            @Override
            public void execute(String response) {
                activeRequests--;
                callback.execute(response);
            }
        };
        ServerRequest request = new ServerRequest(METHOD_PUT, "api/songs/" + room.getId() + "/" + song, "", cb, null);
        new ConnectionManager().execute(request);
        activeRequests++;
    }

    @Override
    public void getCurrentSong(RoomInfo room, final ICallback callback) {
        ICallback cb = new ICallback() {
            @Override
            public void execute(String response) {
                activeRequests--;
                callback.execute(response);
            }
        };
        ServerRequest request = new ServerRequest(METHOD_GET, "api/songs/" + room.getId() + "/current", null, cb, null);
        new ConnectionManager().execute(request);
        activeRequests++;
    }

    @Override
    public void getSongs(RoomInfo room, final ICallback callback) {
        ICallback cb = new ICallback() {
            @Override
            public void execute(String response) {
                activeRequests--;
                callback.execute(response);
            }
        };
        ServerRequest request = new ServerRequest(METHOD_GET, "api/songs/" + room.getId(), null, cb, null);
        new ConnectionManager().execute(request);
        activeRequests++;
    }

    @Override
    public void playNextSong(RoomInfo room, final ICallback callback) {
        ICallback cb = new ICallback() {
            @Override
            public void execute(String response) {
                activeRequests--;
                callback.execute(response);
            }
        };
        ServerRequest request = new ServerRequest(METHOD_PUT, "api/songs/" + room.getId() + "/next", "", cb, null);
        new ConnectionManager().execute(request);
        activeRequests++;
    }

    @Override
    public void getLastPlayedSongs(RoomInfo room, int count, final ICallback callback) {
        ICallback cb = new ICallback() {
            @Override
            public void execute(String response) {
                activeRequests--;
                callback.execute(response);
            }
        };
        ServerRequest request = new ServerRequest(METHOD_GET, "api/songs/" + room.getId() + "/last/" + count, null, cb, null);
        new ConnectionManager().execute(request);
        activeRequests++;
    }

    @Override
    public void getLeftSongCount(RoomInfo room, final ICallback callback) {
        ICallback cb = new ICallback() {
            @Override
            public void execute(String response) {
                activeRequests--;
                callback.execute(response);
            }
        };
        ServerRequest request = new ServerRequest(METHOD_GET, "api/songs/" + room.getId() + "/left", null, cb, null);
        new ConnectionManager().execute(request);
        activeRequests++;
    }

    @Override
    public void voteSkipSong(RoomInfo room, final ICallback callback) {
        ICallback cb = new ICallback() {
            @Override
            public void execute(String response) {
                activeRequests--;
                callback.execute(response);
            }
        };
        ServerRequest request = new ServerRequest(METHOD_PUT, "api/songs/" + room.getId(), "", cb, null);
        new ConnectionManager().execute(request);
        activeRequests++;
    }

    @Override
    public void setSkipThreshold(RoomInfo room, double threshold, final ICallback callback) {
        ICallback cb = new ICallback() {
            @Override
            public void execute(String response) {
                activeRequests--;
                callback.execute(response);
            }
        };
        ServerRequest request = new ServerRequest(METHOD_PUT, "api/rooms/" + room.getId(), Double.toString(threshold), cb, null);
        new ConnectionManager().execute(request);
        activeRequests++;
    }

    @Override
    public void waitForAllRequests() {
        while(activeRequests > 0){
        }
    }
}
