package com.latenightpenguin.groupdj.NetworkServices.ServerAPI.Requests;

import com.latenightpenguin.groupdj.NetworkServices.ServerAPI.ICallback;
import com.latenightpenguin.groupdj.RoomInfo;


public class RequestsHelper {
    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_DELETE = "DELETE";

    private int activeRequests;

    public void registerUser(String user, final ICallback callback) {
        ICallback cb = new ICallback() {
            @Override
            public void execute(String response) {
                activeRequests--;
                callback.execute(response);
            }
        };
        ServerRequest request = new ServerRequest(METHOD_POST, "api/users", String.format("\"%s\"", user), cb, null);
        new ConnectionManager().execute(request);
        activeRequests++;
    }

    public void createRoom(String user, final ICallback callback) {
        ICallback cb = new ICallback() {
            @Override
            public void execute(String response) {
                activeRequests--;
                callback.execute(response);
            }
        };
        ServerRequest request = new ServerRequest(METHOD_POST, "api/rooms", String.format("\"%s\"", user), cb, null);
        new ConnectionManager().execute(request);
        activeRequests++;
    }

    public void connectToRoom(String user, int loginCode, final ICallback callback) {
        ICallback cb = new ICallback() {
            @Override
            public void execute(String response) {
                activeRequests--;
                callback.execute(response);
            }
        };
        ServerRequest request = new ServerRequest(METHOD_POST, "api/rooms", String.format("\"%s\"", user), cb, null);
        new ConnectionManager().execute(request);
        activeRequests++;
    }

    public void disconnectFromRoom(String user, final ICallback callback) {
        ICallback cb = new ICallback() {
            @Override
            public void execute(String response) {
                activeRequests--;
                callback.execute(response);
            }
        };
        ServerRequest request = new ServerRequest(METHOD_PUT, "api/rooms", "\"" + user + "\"", cb, null);
        new ConnectionManager().execute(request);
        activeRequests++;
    }

    public void addSong(RoomInfo room, String song, final ICallback callback) {
        ICallback cb = new ICallback() {
            @Override
            public void execute(String response) {
                activeRequests--;
                callback.execute(response);
            }
        };
        ServerRequest request = new ServerRequest(METHOD_PUT, String.format("api/songs/%d/%s", room.getId(), song), "", cb, null);
        new ConnectionManager().execute(request);
        activeRequests++;
    }

    public void getCurrentSong(RoomInfo room, final ICallback callback) {
        ICallback cb = new ICallback() {
            @Override
            public void execute(String response) {
                activeRequests--;
                callback.execute(response);
            }
        };
        ServerRequest request = new ServerRequest(METHOD_GET, String.format("api/songs/%d/current", room.getId()), null, cb, null);
        new ConnectionManager().execute(request);
        activeRequests++;
    }

    public void getSongs(RoomInfo room, final ICallback callback) {
        ICallback cb = new ICallback() {
            @Override
            public void execute(String response) {
                activeRequests--;
                callback.execute(response);
            }
        };
        ServerRequest request = new ServerRequest(METHOD_GET, String.format("api/songs/%d", room.getId()), null, cb, null);
        new ConnectionManager().execute(request);
        activeRequests++;
    }

    public void playNextSong(RoomInfo room, final ICallback callback) {
        ICallback cb = new ICallback() {
            @Override
            public void execute(String response) {
                activeRequests--;
                callback.execute(response);
            }
        };
        ServerRequest request = new ServerRequest(METHOD_PUT, String.format("api/songs/%d/next", room.getId()), "", cb, null);
        new ConnectionManager().execute(request);
        activeRequests++;
    }

    public void getLastPlayedSongs(RoomInfo room, int count, final ICallback callback) {
        ICallback cb = new ICallback() {
            @Override
            public void execute(String response) {
                activeRequests--;
                callback.execute(response);
            }
        };
        ServerRequest request = new ServerRequest(METHOD_GET, String.format("api/songs/%d/last/%d", room.getId(), count), null, cb, null);
        new ConnectionManager().execute(request);
        activeRequests++;
    }

    public void getLeftSongCount(RoomInfo room, final ICallback callback) {
        ICallback cb = new ICallback() {
            @Override
            public void execute(String response) {
                activeRequests--;
                callback.execute(response);
            }
        };
        ServerRequest request = new ServerRequest(METHOD_GET, String.format("api/songs/%d/left", room.getId()), null, cb, null);
        new ConnectionManager().execute(request);
        activeRequests++;
    }

    public void voteSkipSong(RoomInfo room, final ICallback callback) {
        ICallback cb = new ICallback() {
            @Override
            public void execute(String response) {
                activeRequests--;
                callback.execute(response);
            }
        };
        ServerRequest request = new ServerRequest(METHOD_PUT, String.format("api/songs/%d", room.getId()), "", cb, null);
        new ConnectionManager().execute(request);
        activeRequests++;
    }

    public void setSkipThreshold(RoomInfo room, double threshold, final ICallback callback) {
        ICallback cb = new ICallback() {
            @Override
            public void execute(String response) {
                activeRequests--;
                callback.execute(response);
            }
        };
        ServerRequest request = new ServerRequest(METHOD_PUT, String.format("api/rooms/%d", room.getId()), Double.toString(threshold), cb, null);
        new ConnectionManager().execute(request);
        activeRequests++;
    }

    public void waitForAllRequests() {
        while(activeRequests > 0){
        }
    }
}
