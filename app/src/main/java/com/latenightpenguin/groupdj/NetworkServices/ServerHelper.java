package com.latenightpenguin.groupdj.NetworkServices;

import android.os.AsyncTask;
import android.telecom.Call;
import android.util.Log;
import android.widget.TextView;

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

public class ServerHelper {
    private static final String SERVER_URL = "https://group-dj-app.herokuapp.com/";
    private static final String METHOD_GET = "GET";
    private static final String METHOD_POST = "POST";
    private static final String METHOD_PUT = "PUT";
    public static final String CONNECTION_ERROR = "There was error connecting to the server";
    public static final String RESPONSE_ERROR = "There was error getting response";

    public ServerHelper() {
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
     * Creates new room and connects to it.
     *
     * @param id user id.
     * @param uiElement ui element to update after finishing task.
     */
    public void createRoom(final RoomInfo room, final String id, final TextView uiElement) {
        final ServerRequest.Callback callback = new ServerRequest.Callback() {
            TextView view = uiElement;

            @Override
            public void execute(String response) {
                if(response != null) {
                    if(response.equals(CONNECTION_ERROR) || response.equals(RESPONSE_ERROR)){
                        uiElement.setText(response);
                    }

                    try {
                        JSONObject roomInfo = new JSONObject(response.toString());
                        int roomId = roomInfo.getInt("id");
                        int loginCode = roomInfo.getInt("logincode");

                        room.setId(roomId);
                        room.setLoginCode(loginCode);

                        uiElement.setText("Login code is " + String.valueOf(loginCode));
                    } catch (JSONException e) {
                        Log.d("MusicDJ", response.toString());
                        uiElement.setText("Error parsing response");
                        e.printStackTrace();
                    }
                }
            }
        };

        createRoom(id, callback);
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
     * Registers user and creates room
     * @param id users email
     * @param uiElement ui element for login code
     */
    public void registerUser(final RoomInfo room, final String id, final TextView uiElement) {
        final ServerRequest.Callback callback = new ServerRequest.Callback() {

            @Override
            public void execute(String response) {
                createRoom(room, id, uiElement);
            }
        };

        registerUser(id, callback);
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

    /**
     * Connects user and connects him to room
     * @param room Room info.
     * @param id user id.
     * @param uiElement ui element to update.
     */
    public void connectUser(final RoomInfo room, final String id, final TextView uiElement) {
        final ServerRequest.Callback callback = new ServerRequest.Callback() {
            TextView view = uiElement;

            @Override
            public void execute(String response) {
                connectToRoom(room, id, uiElement);
            }
        };

        connectUser(id, callback);
    }

    public void connectToRoom(int roomLoginCode, String id, ServerRequest.Callback callback) {
        String body = "{ \"email\": \"" + id + "\", \"logincode\": " + roomLoginCode + " }";
        Log.d("MusicDJ", body);

        ServerRequest request = new ServerRequest(METHOD_PUT, "api/users", body, callback, null);
        new ConnectionManager().execute(request);
    }

    /**
     * Connects user to room
     * @param room room info.
     * @param id user id.
     * @param uiElement ui element to update.
     */
    public void connectToRoom(final RoomInfo room, final String id, final TextView uiElement) {
        final ServerRequest.Callback callback = new ServerRequest.Callback() {
            TextView view = uiElement;

            @Override
            public void execute(String response) {
                if(response != null) {
                    if(response.equals(CONNECTION_ERROR) || response.equals(RESPONSE_ERROR)){
                        uiElement.setText(response);
                    }

                    try {
                        JSONObject roomInfo = new JSONObject(response.toString());
                        int roomId = roomInfo.getInt("id");
                        int loginCode = roomInfo.getInt("logincode");

                        room.setId(roomId);
                        room.setLoginCode(loginCode);

                        uiElement.setText("Login code is " + String.valueOf(loginCode));
                    } catch (JSONException e) {
                        Log.d("MusicDJ", response.toString());
                        uiElement.setText("Not connected");
                        e.printStackTrace();
                    }
                }
            }
        };

        connectToRoom(room.getLoginCode(), id, callback);
    }

    public void getSongs(final RoomInfo room, ServerRequest.Callback callback) {
        Log.d("MusicDJ", "api/rooms/" + room.getId() + "/songs");
        ServerRequest request = new ServerRequest(METHOD_GET, "api/rooms/" + room.getId() + "/songs", null, callback, null);
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

    /**
     * Gets songs from room
     *
     * @param room room info.
     * @param songIds list where to put songs
     */
    public void getSongs(final RoomInfo room, final List<String> songIds) {
        final ServerRequest.Callback callback = new ServerRequest.Callback() {
            @Override
            public void execute(String response) {
                if(response != null || response != "") {
                    if(response.equals(CONNECTION_ERROR) || response.equals(RESPONSE_ERROR)){
                    }

                    try {
                        JSONArray array = new JSONArray(response);

                        for(int i = 0; i < array.length(); i++) {
                            JSONObject song = array.getJSONObject(i);
                            songIds.add(song.getString("id"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        getSongs(room, callback);
    }

    public void playNextSong(final RoomInfo room, ServerRequest.Callback callback) {
        Log.d("MusicDJ", "api/rooms/" + room.getId() + "/next");
        ServerRequest request = new ServerRequest(METHOD_PUT, "api/rooms/" + room.getId() + "/next", "", callback, null);
        new ConnectionManager().execute(request);
    }

    public void playNextSong(final RoomInfo room, final StringBuilder song) {
        final ServerRequest.Callback callback = new ServerRequest.Callback() {
            @Override
            public void execute(String response) {
                if(response != null || response != "") {
                    if(response.equals(CONNECTION_ERROR) || response.equals(RESPONSE_ERROR)){
                    }

                    try {
                        JSONObject songObject = new JSONObject(response);

                        song.append(songObject.getString("id"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        playNextSong(room, callback);
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
        ServerRequest request = new ServerRequest(METHOD_PUT, "api/rooms/" + room.getId() + "/" + song, "", callback, null);
        new ConnectionManager().execute(request);
    }

    public void addSong(final RoomInfo room, final String song) {
        final ServerRequest.Callback callback = new ServerRequest.Callback() {
            @Override
            public void execute(String response) {

            }
        };

        addSong(room, song, callback);
    }

    private class ConnectionManager extends AsyncTask<ServerRequest, Void, String> {
        ServerRequest[] requests;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(ServerRequest... requests) {
            this.requests = requests;
            InputStream response;
            StringBuilder responseString = new StringBuilder();

            try {
                URL url;
                if(requests[0].getArguments() != null && requests[0].getArguments().length > 0) {
                    url = new URL(SERVER_URL + requests[0].getPath() + requests[0].getArgumentsFormatted());
                } else {
                    url = new URL(SERVER_URL + requests[0].getPath());
                }

                Log.d("MusicDJAsync", url.toString());

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod(requests[0].getMethod());
                connection.setRequestProperty("Content-Type", "application/json");

                if(requests[0].getBody() != null) {
                    connection.setDoOutput(true);

                    OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
                    BufferedWriter writer = new BufferedWriter(out);
                    writer.write(requests[0].getBody());
                    writer.close();
                    out.close();
                }

                connection.connect();

                Log.d("MusicDJ", "Response code: " + connection.getResponseCode());
                if(connection.getResponseCode() != 200) {
                    response = new ByteArrayInputStream(CONNECTION_ERROR.getBytes(StandardCharsets.UTF_8));
                } else {
                    response = connection.getInputStream();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(response));

                    String line = "";
                    try{
                        while((line = reader.readLine()) != null){
                            responseString.append(line).append('\n');
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            response.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IOException e) {
                Log.d("MusicDJ", e.getMessage());
                responseString.append(RESPONSE_ERROR);
                e.printStackTrace();
            }

            return responseString.toString();
        }

        @Override
        protected void onPostExecute(String result){
            if(requests[0].getCallback() != null) {
                requests[0].getCallback().execute(result);
            }
        }
    }
}
