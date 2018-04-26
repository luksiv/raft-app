package com.latenightpenguin.groupdj.NetworkServices.ServerAPI;

import com.latenightpenguin.groupdj.RoomInfo;

public interface IServerHelper {
    void registerUser(String user, ICallback callback);
    void createRoom(String user, ICallback callback);
    void connectToRoom(String user, int loginCode, ICallback callback);
    void addSong(RoomInfo room, String song, ICallback callback);
    void getCurrentSong(RoomInfo room, ICallback callback);
    void getSongs(RoomInfo room, ICallback callback);
    void playNextSong(RoomInfo room, ICallback callback);
    void getLastPlayedSongs(RoomInfo room, int count, ICallback callback);
    void getLeftSongCount(RoomInfo room, ICallback callback);
    void voteSkipSong(RoomInfo room, ICallback callback);
    void setSkipThreshold(RoomInfo room, double threshold, ICallback callback);
    void waitForAllRequests();
    void connectWebSocket();
    void closeWebSocket();
    WebSocketStatus getWebSocketStatus();
    void setRoomUpdates(int id);
    void announcePlayTime(long milliseconds);
    void announcePause(long milliseconds);
    void setPlayingNextCallback(ICallback playingNextCallback);
    void setSongAddedCallback(ICallback songAddedCallback);
    void setSongPausedCallback(ICallback songPausedCallback);
    void setSongPlayTimeCallback(ICallback songPlayTimeCallback);
    void setSongSkippedCallback(ICallback songSkipedCallback);
    void setConnectedToRoomCallback(ICallback connectedToRoomCallback);
}
