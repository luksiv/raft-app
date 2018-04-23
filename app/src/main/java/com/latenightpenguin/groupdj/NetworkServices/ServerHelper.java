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
    //public static final String SERVER_URL = "https://group-dj-app.herokuapp.com/";
    //private static final String SERVER_WEBSOCKET_URL = "ws://group-dj-app.herokuapp.com/realtime";
    public static final String SERVER_URL = "http://192.168.0.39:61135/";
    private static final String SERVER_WEBSOCKET_URL = "ws://192.168.0.39:61135/realtime";
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

    // websocket callbacks
    private ServerRequest.Callback playingNextCallback;
    private ServerRequest.Callback songAddedCallback;
    private ServerRequest.Callback songPausedCallback;
    private ServerRequest.Callback songPlayTimeCallback;

    public ServerHelper() {
        client = new OkHttpClient();
        roomId = -1;
        status = WebSocketStatus.DISCONNECTED;
    }

    /**
     * Creates new room and connects to it.
     *
     * @param id user id.
     * @param callback callback to execute.
     */
    public void createRoom(final String id, final ServerRequest.Callback callback) {
        ServerRequest request = new ServerRequest(METHOD_POST, "api/rooms", "\"" + id + "\"", callback, null);
        new ConnectionManager().execute(request);
    }

    /**
     * Registers user and executes callback
     *
     * @param id user id
     * @param callback callback to execute
     */
    public void registerUser(final String id, final ServerRequest.Callback callback) {
        ServerRequest request = new ServerRequest(METHOD_POST, "api/users", "\"" + id + "\"", callback, null);
        new ConnectionManager().execute(request);
    }

    /**
     * Connects user to room.
     *
     * @param id user id.
     * @param callback callback to execute.
     */
    public void connectUser(final String id, final ServerRequest.Callback callback) {
        ServerRequest request = new ServerRequest(METHOD_POST, "api/users", "\"" + id + "\"", callback, null);
        new ConnectionManager().execute(request);
    }

    public void connectToRoom(int roomLoginCode, String id, ServerRequest.Callback callback) {
        String body = "{ \"email\": \"" + id + "\", \"logincode\": " + roomLoginCode + " }";
        Log.d("MusicDJ", body);

        ServerRequest request = new ServerRequest(METHOD_PUT, "api/users", body, callback, null);
        new ConnectionManager().execute(request);
    }

    public void getSongs(final RoomInfo room, ServerRequest.Callback callback) {
        Log.d("MusicDJ", "api/songs/" + room.getId());
        ServerRequest request = new ServerRequest(METHOD_GET, "api/songs/" + room.getId(), null, callback, null);
        new ConnectionManager().execute(request);
    }

    public ArrayList<String> convertToList(String response) {
        ArrayList<String> songs = new ArrayList<>();

        if(response != null || response != "") {
            if(response.equals(CONNECTION_ERROR) || response.equals(RESPONSE_ERROR)){
            }

            try {
                JSONArray array = new JSONArray(response);

                for(int i = 0; i < array.length(); i++) {
                    JSONObject song = array.getJSONObject(i);
                    songs.add(song.getString("id"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return songs;
    }

    public void playNextSong(final RoomInfo room, ServerRequest.Callback callback) {
        Log.d("MusicDJ", "api/songs/" + room.getId() + "/next");
        ServerRequest request = new ServerRequest(METHOD_PUT, "api/songs/" + room.getId() + "/next", "", callback, null);
        new ConnectionManager().execute(request);
    }

    public String getSongId(String response) {
        String songId = "";

        if(response != null || response != "") {
            if(response.equals(CONNECTION_ERROR) || response.equals(RESPONSE_ERROR)){
            }

            try {
                JSONObject songObject = new JSONObject(response);

                songId = songObject.getString("id");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return songId;
    }

    public void addSong(RoomInfo room, final String song, ServerRequest.Callback callback) {
        ServerRequest request = new ServerRequest(METHOD_PUT, "api/songs/" + room.getId() + "/" + song, "", callback, null);
        new ConnectionManager().execute(request);
    }

    public void getCurrentSong(RoomInfo room, ServerRequest.Callback callback) {
        ServerRequest request = new ServerRequest(METHOD_GET, "api/songs/" + room.getId() + "/current", null, callback, null);
        new ConnectionManager().execute(request);
    }

    public void getLastPlayedSongs(RoomInfo room, int count, ServerRequest.Callback callback) {
        ServerRequest request = new ServerRequest(METHOD_GET, "api/songs/" + room.getId() + "/last/" + count, null, callback, null);
        new ConnectionManager().execute(request);
    }

    public void getLeftSongCount(RoomInfo room, ServerRequest.Callback callback) {
        ServerRequest request = new ServerRequest(METHOD_GET, "api/songs/" + room.getId() + "/left", null, callback, null);
        new ConnectionManager().execute(request);
    }

    public void connectWebSocket() {
        Request request = new Request.Builder().url(SERVER_WEBSOCKET_URL).build();
        ServerListener serverListener = new ServerListener();
        serverListener.setMessageHandler(messageHandler);
        serverListener.setErrorHandler(errorHandler);
        websocket = client.newWebSocket(request, serverListener);
        status = WebSocketStatus.CONNECTED;
        if(roomId != -1){
            setRoomUpdates(roomId);
        }
    }

    public void setRoomUpdates(int id) {
        roomId = id;
        if(status == WebSocketStatus.CONNECTED) {
            websocket.send("room;" + id);
        }
    }

    public void announcePlayTime(int milliseconds) {
        if(status == WebSocketStatus.CONNECTED) {
            websocket.send("play;" + milliseconds);
        }
    }

    public void announcePause(int milliseconds) {
        if(status == WebSocketStatus.CONNECTED) {
            websocket.send("pause;" + milliseconds);
        }
    }

    public void closeWebSocket(){
        if(status == WebSocketStatus.CONNECTED) {
            websocket.close(0, "");
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

    private ServerListener.MessageHandler messageHandler = new ServerListener.MessageHandler() {
        @Override
        public void handle(String message) {
            switch(message) {
                case "song added":
                    if(songAddedCallback != null) {
                        songAddedCallback.execute("");
                    }
                break;
                case "next song":
                    if(playingNextCallback != null) {
                        playingNextCallback.execute("");
                    }
                break;
                default:
                String[] strings = message.split(";");
                if(strings[0].equals("paused")) {
                    if(songPausedCallback != null) {
                        songPausedCallback.execute(strings[1]);
                    }
                } else {
                    if(songPlayTimeCallback != null) {
                        songPlayTimeCallback.execute(message);
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
            if(message.equals("failed")) {
                connectWebSocket();
            }
        }
    };

    public WebSocketStatus getStatus() {
        return status;
    }

    public enum WebSocketStatus {
        CONNECTED, DISCONNECTED;
    };
}
