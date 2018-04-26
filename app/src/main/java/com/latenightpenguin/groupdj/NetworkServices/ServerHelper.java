package com.latenightpenguin.groupdj.NetworkServices;

import android.os.AsyncTask;
import android.telecom.Call;
import android.util.Log;
import android.widget.MediaController;
import android.widget.TextView;

import com.latenightpenguin.groupdj.NetworkServices.ServerWebSockets.ServerListener;
import com.latenightpenguin.groupdj.RoomInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;

public class ServerHelper {
    public static String SERVER_URL;
    private static String SERVER_WEBSOCKET_URL;
    private static final String METHOD_GET = "GET";
    private static final String METHOD_POST = "POST";
    private static final String METHOD_PUT = "PUT";
    private static final String METHOD_DELETE = "DELETE";
    public static final String CONNECTION_ERROR = "There was error connecting to the server";
    public static final String RESPONSE_ERROR = "There was error getting response";

    private OkHttpClient client;
    private WebSocket websocket;
    private WebSocketStatus status;
    private int roomId;
    private int activeRequests;

    // websocket callbacks
    private ServerRequest.Callback playingNextCallback;
    private ServerRequest.Callback songAddedCallback;
    private ServerRequest.Callback songPausedCallback;
    private ServerRequest.Callback songPlayTimeCallback;
    private ServerRequest.Callback songSkipedCallback;
    private ServerRequest.Callback connectedToRoomCallback;

    public ServerHelper(String url) {
        SERVER_URL = "http://" + url;
        SERVER_WEBSOCKET_URL = "ws://" + url;
        client = new OkHttpClient();
        roomId = -1;
        status = WebSocketStatus.DISCONNECTED;
        activeRequests = 0;
    }

    public boolean areAllRequestsFinished() {
        return activeRequests == 0;
    }

    //region Converters
    public String getSongId(String response) {
        String songId = "";

        if (response != null && !response.equals("")) {
            if (response.equals(CONNECTION_ERROR) || response.equals(RESPONSE_ERROR)) {
            }

            try {
                JSONObject songObject = new JSONObject(response);

                songId = songObject.getString("song");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return songId;
    }

    public ArrayList<String> convertToList(String response) {
        ArrayList<String> songs = new ArrayList<>();

        if (response != null && !response.equals("")) {
            if (response.equals(CONNECTION_ERROR) || response.equals(RESPONSE_ERROR)) {
            }

            try {
                JSONArray array = new JSONArray(response);

                for (int i = 0; i < array.length(); i++) {
                    JSONObject song = array.getJSONObject(i);
                    songs.add(song.getString("song"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return songs;
    }
    //endregion

    //region Requests

    /**
     * Creates new room and connects to it.
     *
     * @param id       user id.
     * @param callback callback to execute.
     */
    public void createRoom(final String id, final ServerRequest.Callback callback) {
        ServerRequest.Callback cb = new ServerRequest.Callback() {
            @Override
            public void execute(String response) {
                activeRequests--;
                callback.execute(response);
            }
        };
        ServerRequest request = new ServerRequest(METHOD_POST, "api/rooms", "\"" + id + "\"", cb, null);
        new ConnectionManager().execute(request);
        activeRequests++;
    }

    /**
     * Registers user and executes callback
     *
     * @param id       user id
     * @param callback callback to execute
     */
    public void registerUser(final String id, final ServerRequest.Callback callback) {
        ServerRequest.Callback cb = new ServerRequest.Callback() {
            @Override
            public void execute(String response) {
                activeRequests--;
                callback.execute(response);
            }
        };
        ServerRequest request = new ServerRequest(METHOD_POST, "api/users", "\"" + id + "\"", cb, null);
        new ConnectionManager().execute(request);
        activeRequests++;
    }

    /**
     * Connects user to room.
     *
     * @param id       user id.
     * @param callback callback to execute.
     */
    public void connectUser(final String id, final ServerRequest.Callback callback) {
        ServerRequest.Callback cb = new ServerRequest.Callback() {
            @Override
            public void execute(String response) {
                activeRequests--;
                callback.execute(response);
            }
        };
        ServerRequest request = new ServerRequest(METHOD_POST, "api/users", "\"" + id + "\"", cb, null);
        new ConnectionManager().execute(request);
        activeRequests++;
    }

    public void connectToRoom(int roomLoginCode, String id, final ServerRequest.Callback callback) {
        String body = "{ \"email\": \"" + id + "\", \"logincode\": " + roomLoginCode + " }";
        Log.d("MusicDJ", body);

        ServerRequest.Callback cb = new ServerRequest.Callback() {
            @Override
            public void execute(String response) {
                activeRequests--;
                callback.execute(response);
            }
        };

        ServerRequest request = new ServerRequest(METHOD_PUT, "api/users", body, cb, null);
        new ConnectionManager().execute(request);
        activeRequests++;
    }

    public void getSongs(final RoomInfo room, final ServerRequest.Callback callback) {
        Log.d("MusicDJ", "api/songs/" + room.getId());
        ServerRequest.Callback cb = new ServerRequest.Callback() {
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

    public void playNextSong(final RoomInfo room, final ServerRequest.Callback callback) {
        Log.d("MusicDJ", "api/songs/" + room.getId() + "/next");
        ServerRequest.Callback cb = new ServerRequest.Callback() {
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

    public void addSong(RoomInfo room, final String song, final ServerRequest.Callback callback) {
        ServerRequest.Callback cb = new ServerRequest.Callback() {
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

    public void getCurrentSong(RoomInfo room, final ServerRequest.Callback callback) {
        ServerRequest.Callback cb = new ServerRequest.Callback() {
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

    public void getLastPlayedSongs(RoomInfo room, int count, final ServerRequest.Callback callback) {
        /*ServerRequest.Callback cb = new ServerRequest.Callback() {
            @Override
            public void execute(String response) {
                activeRequests--;
                callback.execute(response);
            }
        };
        ServerRequest request = new ServerRequest(METHOD_GET, "api/songs/" + room.getId() + "/last/" + count, null, cb, null);
        new ConnectionManager().execute(request);
        activeRequests++;*/
    }

    public void getLeftSongCount(RoomInfo room, final ServerRequest.Callback callback) {
        ServerRequest.Callback cb = new ServerRequest.Callback() {
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

    public void voteSkipSong(RoomInfo room, final ServerRequest.Callback callback) {
        ServerRequest.Callback cb = new ServerRequest.Callback() {
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

    public void setVoteThreshold(RoomInfo room, double threshold, final ServerRequest.Callback callback) {
        ServerRequest.Callback cb = new ServerRequest.Callback() {
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
    //endregion

    //region WebSockets
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

    public void setRoomUpdates(int id) {
        roomId = id;
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

    public void setPlayingNextCallback(ServerRequest.Callback playingNextCallback) {
        this.playingNextCallback = playingNextCallback;
    }

    public void setSongAddedCallback(ServerRequest.Callback songAddedCallback) {
        this.songAddedCallback = songAddedCallback;
    }

    public void setSongPausedCallback(ServerRequest.Callback songPausedCallback) {
        this.songPausedCallback = songPausedCallback;
    }

    public void setSongPlayTimeCallback(ServerRequest.Callback songPlayTimeCallback) {
        this.songPlayTimeCallback = songPlayTimeCallback;
    }

    public void setSongSkippedCallback(ServerRequest.Callback songSkipedCallback) {
        this.songSkipedCallback = songSkipedCallback;
    }

    public void setConnectedToRoomCallback(ServerRequest.Callback connectedToRoomCallback) {
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

    public WebSocketStatus getStatus() {
        return status;
    }

    public enum WebSocketStatus {
        CONNECTED, DISCONNECTED
    }
    //endregion
}
