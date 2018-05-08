package com.latenightpenguin.groupdj.NetworkServices.ServerAPI;

import com.latenightpenguin.groupdj.NetworkServices.ServerAPI.Requests.IRequestCallback;
import com.latenightpenguin.groupdj.NetworkServices.ServerAPI.Requests.RequestsHelper;
import com.latenightpenguin.groupdj.NetworkServices.ServerAPI.WebSockets.IWebSocketCallback;
import com.latenightpenguin.groupdj.NetworkServices.ServerAPI.WebSockets.WebSocketHelper;

public class ServerHelper implements IServerHelper{
    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_DELETE = "DELETE";
    public static final String CONNECTION_ERROR = "There was error connecting to the server";
    public static final String RESPONSE_ERROR = "There was error getting response";

    private RequestsHelper requestsHelper;
    private WebSocketHelper webSocketHelper;

    // websocket callbacks
    private IWebSocketCallback playingNextCallback;
    private IWebSocketCallback songAddedCallback;
    private IWebSocketCallback songPausedCallback;
    private IWebSocketCallback songPlayTimeCallback;
    private IWebSocketCallback songSkippedCallback;
    private IWebSocketCallback connectedToRoomCallback;

    public ServerHelper(String url) {
        requestsHelper = new RequestsHelper("http://" + url);
        webSocketHelper = new WebSocketHelper("ws://" + url);
    }

    //region WebSockets
    @Override
    public void connectWebSocket() {
        webSocketHelper.connectWebSocket();
    }

    @Override
    public void setRoomUpdates(int id) {
        webSocketHelper.setRoomUpdates(id);
    }

    @Override
    public void announcePlayTime(long milliseconds) {
        webSocketHelper.announcePlayTime(milliseconds);
    }

    @Override
    public void announcePause(long milliseconds) {
        webSocketHelper.announcePause(milliseconds);
    }

    @Override
    public void closeWebSocket() {
        webSocketHelper.closeWebSocket();
    }

    @Override
    public void setPlayingNextCallback(IWebSocketCallback playingNextCallback) {
        webSocketHelper.setPlayingNextCallback(playingNextCallback);
    }

    @Override
    public void setSongAddedCallback(IWebSocketCallback songAddedCallback) {
        webSocketHelper.setSongAddedCallback(songAddedCallback);
    }

    @Override
    public void setSongPausedCallback(IWebSocketCallback songPausedCallback) {
        webSocketHelper.setSongPausedCallback(songPausedCallback);
    }

    @Override
    public void setSongPlayTimeCallback(IWebSocketCallback songPlayTimeCallback) {
        webSocketHelper.setSongPlayTimeCallback(songPlayTimeCallback);
    }

    @Override
    public void setSongSkippedCallback(IWebSocketCallback songSkippedCallback) {
        webSocketHelper.setSongSkippedCallback(songSkippedCallback);
    }

    @Override
    public void setConnectedToRoomCallback(IWebSocketCallback connectedToRoomCallback) {
        webSocketHelper.setConnectedToRoomCallback(connectedToRoomCallback);
    }
    //endregion

    @Override
    public WebSocketStatus getWebSocketStatus() {
        return webSocketHelper.getStatus();
    }

    @Override
    public void registerUser(String user, final IRequestCallback callback) {
        requestsHelper.registerUser(user, callback);
    }

    @Override
    public void createRoom(String user, final IRequestCallback callback) {
        requestsHelper.createRoom(user, callback);
    }

    @Override
    public void connectToRoom(String user, int loginCode, final IRequestCallback callback) {
        requestsHelper.connectToRoom(user, loginCode, callback);
    }

    @Override
    public void disconnectFromRoom(int room, String user, final IRequestCallback callback) {
        requestsHelper.disconnectFromRoom(room, user, callback);
    }

    @Override
    public void addSong(RoomInfo room, String song, final IRequestCallback callback) {
        requestsHelper.addSong(room, song, callback);
    }

    @Override
    public void getCurrentSong(RoomInfo room, final IRequestCallback callback) {
        requestsHelper.getCurrentSong(room, callback);
    }

    @Override
    public void getSongs(RoomInfo room, final IRequestCallback callback) {
        requestsHelper.getSongs(room, callback);
    }

    @Override
    public void playNext(RoomInfo room, String song, final IRequestCallback callback) {
        requestsHelper.playNext(room, song, callback);
    }

    @Override
    public void playNextSong(RoomInfo room, final IRequestCallback callback) {
        requestsHelper.playNextSong(room, callback);
    }

    @Override
    public void getLastPlayedSongs(RoomInfo room, int count, final IRequestCallback callback) {
        requestsHelper.getLastPlayedSongs(room, count, callback);
    }

    @Override
    public void getLeftSongCount(RoomInfo room, final IRequestCallback callback) {
        requestsHelper.getLeftSongCount(room, callback);
    }

    @Override
    public void voteSkipSong(RoomInfo room, final IRequestCallback callback) {
        requestsHelper.voteSkipSong(room, callback);
    }

    @Override
    public void setSkipThreshold(RoomInfo room, double threshold, final IRequestCallback callback) {
        requestsHelper.setSkipThreshold(room, threshold, callback);
    }

    @Override
    public void waitForAllRequests() {
        requestsHelper.waitForAllRequests();
    }
}
