package com.latenightpenguin.groupdj.NetworkServices.ServerAPI.Requests;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.latenightpenguin.groupdj.NetworkServices.ServerAPI.RoomInfo;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RequestsHelper {
    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_DELETE = "DELETE";
    public static final MediaType MEDIA_TYPE = MediaType.parse("application/json");

    public static String serverUrl;
    private int activeRequests;
    private OkHttpClient client;

    public RequestsHelper(String url){
        serverUrl = url;
        activeRequests = 0;
        client = new OkHttpClient();
    }

    public void registerUser(String user, final IRequestCallback callback) {
        String url = serverUrl + "api/users";
        Callback cb = GetCallback(callback);
        RequestBody body = RequestBody.create(MEDIA_TYPE, String.format("\"%s\"", user));
        final Request request = new Request.Builder()
                .header("Content-Type", "application/json")
                .url(url)
                .post(body)
                .build();

        Log.d("ServerAPI", url);
        client.newCall(request).enqueue(cb);
        activeRequests++;
    }

    public void createRoom(String user, final IRequestCallback callback) {
        String url = serverUrl + "api/rooms";
        Callback cb = GetCallback(callback);
        RequestBody body = RequestBody.create(MEDIA_TYPE, String.format("\"%s\"", user));
        final Request request = new Request.Builder()
                .header("Content-Type", "application/json")
                .url(url)
                .post(body)
                .build();

        Log.d("ServerAPI", url);
        client.newCall(request).enqueue(cb);
        activeRequests++;
    }

    public void connectToRoom(String user, int loginCode, final IRequestCallback callback) {
        String url = serverUrl + "api/users";
        Callback cb = GetCallback(callback);
        RequestBody body = RequestBody.create(MEDIA_TYPE, String.format("{ \"email\": \"%s\", \"logincode\": \"%d\" }", user, loginCode));
        final Request request = new Request.Builder()
                .header("Content-Type", "application/json")
                .url(url)
                .put(body)
                .build();

        Log.d("ServerAPI", url);
        client.newCall(request).enqueue(cb);
        activeRequests++;
    }

    public void disconnectFromRoom(String user, final IRequestCallback callback) {
        String url = serverUrl + "api/users/disconnect";
        Callback cb = GetCallback(callback);
        RequestBody body = RequestBody.create(MEDIA_TYPE, String.format("\"%s\"", user));
        final Request request = new Request.Builder()
                .header("Content-Type", "application/json")
                .url(url)
                .put(body)
                .build();

        Log.d("ServerAPI", url);
        client.newCall(request).enqueue(cb);
        activeRequests++;
    }

    public void addSong(RoomInfo room, String song, final IRequestCallback callback) {
        String url = serverUrl + "api/songs/" + room.getId() + "/" + song;
        RequestBody body = RequestBody.create(MEDIA_TYPE, "");
        Callback cb = GetCallback(callback);
        final Request request = new Request.Builder()
                .header("Content-Type", "application/json")
                .url(url)
                .post(body)
                .build();

        Log.d("ServerAPI", url);
        client.newCall(request).enqueue(cb);
        activeRequests++;
    }

    public void getCurrentSong(RoomInfo room, final IRequestCallback callback) {
        String url = serverUrl + "api/songs" + room.getId() + "/current";
        Callback cb = GetCallback(callback);
        final Request request = new Request.Builder()
                .header("Content-Type", "application/json")
                .url(url)
                .build();

        Log.d("ServerAPI", url);
        client.newCall(request).enqueue(cb);
        activeRequests++;
    }

    public void getSongs(RoomInfo room, final IRequestCallback callback) {
        String url = serverUrl + "api/songs" + room.getId();
        Callback cb = GetCallback(callback);
        final Request request = new Request.Builder()
                .header("Content-Type", "application/json")
                .url(url)
                .build();

        Log.d("ServerAPI", url);
        client.newCall(request).enqueue(cb);
        activeRequests++;
    }

    // TODO: change to use new endpoint when it is created
    public void playNext(RoomInfo room, String song, final IRequestCallback callback) {
        String url = serverUrl + "api/songs/" + room.getId() + "/next";
        Callback cb = GetCallback(callback);
        RequestBody body = RequestBody.create(MEDIA_TYPE, "");
        final Request request = new Request.Builder()
                .header("Content-Type", "application/json")
                .url(url)
                .put(body)
                .build();

        Log.d("ServerAPI", url);
        client.newCall(request).enqueue(cb);
        activeRequests++;
    }

    public void playNextSong(RoomInfo room, final IRequestCallback callback) {
        String url = serverUrl + "api/songs/" + room.getId() + "/next";
        Callback cb = GetCallback(callback);
        RequestBody body = RequestBody.create(MEDIA_TYPE, "");
        final Request request = new Request.Builder()
                .header("Content-Type", "application/json")
                .url(url)
                .put(body)
                .build();

        Log.d("ServerAPI", url);
        client.newCall(request).enqueue(cb);
        activeRequests++;
    }

    public void getLastPlayedSongs(RoomInfo room, int count, final IRequestCallback callback) {
        String url = serverUrl + "api/songs/" + room.getId() + "/last/" + count;
        Callback cb = GetCallback(callback);
        final Request request = new Request.Builder()
                .header("Content-Type", "application/json")
                .url(url)
                .build();

        Log.d("ServerAPI", url);
        client.newCall(request).enqueue(cb);
        activeRequests++;
    }

    public void getLeftSongCount(RoomInfo room, final IRequestCallback callback) {
        String url = serverUrl + "api/songs/" + room.getId() + "/left";
        Callback cb = GetCallback(callback);
        final Request request = new Request.Builder()
                .header("Content-Type", "application/json")
                .url(url)
                .build();

        Log.d("ServerAPI", url);
        client.newCall(request).enqueue(cb);
        activeRequests++;
    }

    public void voteSkipSong(RoomInfo room, final IRequestCallback callback) {
        String url = serverUrl + "api/songs/skip/" + room.getId();
        Callback cb = GetCallback(callback);
        RequestBody body = RequestBody.create(MEDIA_TYPE, "");
        final Request request = new Request.Builder()
                .header("Content-Type", "application/json")
                .url(url)
                .put(body)
                .build();

        Log.d("ServerAPI", url);
        client.newCall(request).enqueue(cb);
        activeRequests++;
    }

    public void setSkipThreshold(RoomInfo room, double threshold, final IRequestCallback callback) {
        String url = serverUrl + "api/rooms/" + room.getId();
        Callback cb = GetCallback(callback);
        RequestBody body = RequestBody.create(MEDIA_TYPE, Double.toString(threshold));
        final Request request = new Request.Builder()
                .header("Content-Type", "application/json")
                .url(url)
                .put(body)
                .build();

        Log.d("ServerAPI", url);
        client.newCall(request).enqueue(cb);
        activeRequests++;
    }

    public void waitForAllRequests() {
        while(activeRequests > 0){
        }
    }

    private Callback GetCallback(final IRequestCallback callback){
        final Handler handler = new Handler(Looper.getMainLooper());
        Callback cb = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                activeRequests--;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError(-1, "Error connecting to internet");
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                activeRequests--;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (response.code() == 200) {

                                callback.onSuccess(response.body().string());
                            } else {
                                callback.onError(response.code(), response.body().string());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };

        return cb;
    }
}
