package com.latenightpenguin.groupdj.NetworkServices;

import android.os.AsyncTask;
import android.telecom.Call;
import android.util.Log;
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
    public static final String SERVER_URL = "https://group-dj-app.herokuapp.com/";
    private static final String SERVER_WEBSOCKET_URL = "ws://group-dj-app.herokuapp.com/realtime";
    private static final String METHOD_GET = "GET";
    private static final String METHOD_POST = "POST";
    private static final String METHOD_PUT = "PUT";
    private static final String METHOD_DELETE = "DELETE";
    public static final String CONNECTION_ERROR = "There was error connecting to the server";
    public static final String RESPONSE_ERROR = "There was error getting response";

    private OkHttpClient client;
    private WebSocket websocket;

    // websocket callbacks
    private ServerRequest.Callback playingNextCallback;
    private ServerRequest.Callback songAddedCallback;
    private ServerRequest.Callback songPausedCallback;
    private ServerRequest.Callback songPlayTimeCallback;

    public ServerHelper() {
        client = new OkHttpClient();
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

    public void connectWebSocket() {
        Request request = new Request.Builder().url(SERVER_WEBSOCKET_URL).build();
        ServerListener serverListener = new ServerListener();
        serverListener.setMessageHandler(messageHandler);
        websocket = client.newWebSocket(request, serverListener);
    }

    public void setRoomUpdates(int id) {
        websocket.send("room;" + id);
    }

    public void announcePlayTime(int min, int s) {
        websocket.send("play;" + min + ";" + s);
    }

    public void announcePause(int min, int s) {
        websocket.send("pause;" + min + ";" + s);
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
                String[] firstPart = message.split(":");
                if(firstPart[0].equals("paused")) {
                    if(songPausedCallback != null) {
                        songPausedCallback.execute(firstPart[1] + ":" + firstPart[2]);
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
}
