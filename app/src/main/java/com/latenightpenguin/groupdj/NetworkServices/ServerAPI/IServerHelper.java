package com.latenightpenguin.groupdj.NetworkServices.ServerAPI;

import com.latenightpenguin.groupdj.NetworkServices.ServerAPI.Requests.IRequestCallback;
import com.latenightpenguin.groupdj.NetworkServices.ServerAPI.WebSockets.IWebSocketCallback;

public interface IServerHelper {
    /**
     * Registers user in the server
     * @param user user identifying string
     * @param callback callback which will be executed when request finishes
     */
    void registerUser(String user, IRequestCallback callback);

    /**
     * Creates room with user as a host
     * @param user user who will be host
     * @param callback callback which will be executed when request finishes
     */
    void createRoom(String user, IRequestCallback callback);

    /**
     * Connects room to existing room
     * @param user user's identifier
     * @param loginCode room's login code
     * @param callback callback which will be executed when request finishes
     */
    void connectToRoom(String user, int loginCode, IRequestCallback callback);

    /**
     * Disconnects user from the room
     * @param user user's identifier
     * @param callback callback which will be executed when request finishes
     */
    void disconnectFromRoom(int room, String user, IRequestCallback callback);

    /**
     * Adds song to room's queue
     * @param room room's information
     * @param song song's id
     * @param callback callback which will be executed when request finishes
     */
    void addSong(RoomInfo room, String song, IRequestCallback callback);

    /**
     * Returns currently played song through callback
     * @param room room's information
     * @param callback callback which will be executed when request finishes
     */
    void getCurrentSong(RoomInfo room, IRequestCallback callback);

    /**
     * Gets the list of all songs in room's queue
     * @param room room's information
     * @param callback callback which will be executed when request finishes
     */
    void getSongs(RoomInfo room, IRequestCallback callback);

    /**
     * Plays next song if it exists, otherwise adds song and when plays it.
     * Callback will get json string of song which will be played
     * @param room room's information
     * @param song song which will be played in case no next song exist
     * @param callback callback which will be executed when request finishes
     */
    void playNext(RoomInfo room, String song, IRequestCallback callback);

    /**
     * Plays next song in room's queue
     * @param room room's information
     * @param callback callback which will be executed when request finishes
     */
    void playNextSong(RoomInfo room, IRequestCallback callback);

    /**
     * Returns through callback a number of last played songs
     * @param room room's information
     * @param count specifies how many songs should be returned
     * @param callback callback which will be executed when request finishes
     */
    void getLastPlayedSongs(RoomInfo room, int count, IRequestCallback callback);

    /**
     * Gets how many songs are left in room's queue including currently played
     * @param room room's information
     * @param callback callback which will be executed when request finishes
     */
    void getLeftSongCount(RoomInfo room, IRequestCallback callback);

    /**
     * Votes to skip current song
     * @param room room's information
     * @param callback callback which will be executed when request finishes
     */
    void voteSkipSong(RoomInfo room, IRequestCallback callback);

    /**
     * Sets what percentage of room's users have to vote to skip song that it would be skipped
     * @param room room's information
     * @param threshold number in range (0; 1] that specifies the threshold
     * @param callback callback which will be executed when request finishes
     */
    void setSkipThreshold(RoomInfo room, double threshold, IRequestCallback callback);

    /**
     * blocks thread until all request finish
     */
    void waitForAllRequests();

    /**
     * connects app to server's websocket
     */
    void connectWebSocket();

    /**
     * disconnects app from server's websocket
     */
    void closeWebSocket();

    /**
     * gets current status of websocket connection
     * @return returns connected object if websocket is active, otherwise returns disconnected
     */
    WebSocketStatus getWebSocketStatus();

    /**
     * sets websockets room id
     * @param id room's id
     */
    void setRoomUpdates(int id);

    /**
     * announces current play time of the song through websocket connection.
     * If websocket is disconnected does nothing
     * @param milliseconds play time in miliseconds
     */
    void announcePlayTime(long milliseconds);

    /**
     * announces that song is paused at specified time of the song through websocket connection.
     * If websocket is disconnected does nothing
     * @param milliseconds play time in miliseconds
     */
    void announcePause(long milliseconds);

    /**
     * sets callback for websocket in case play time notification is received
     * @param playingNextCallback callback
     */
    void setPlayingNextCallback(IWebSocketCallback playingNextCallback);

    /**
     * sets callback for websocket in case song added notification is received
     * @param songAddedCallback callback
     */
    void setSongAddedCallback(IWebSocketCallback songAddedCallback);

    /**
     * sets callback for websocket in case song paused notification is received
     * @param songPausedCallback callback
     */
    void setSongPausedCallback(IWebSocketCallback songPausedCallback);

    /**
     * sets callback for websocket in case song play time notification is received
     * @param songPlayTimeCallback callback
     */
    void setSongPlayTimeCallback(IWebSocketCallback songPlayTimeCallback);

    /**
     * sets callback for websocket in case song skipped notification is received
     * @param songSkippedCallback callback
     */
    void setSongSkippedCallback(IWebSocketCallback songSkippedCallback);

    /**
     * sets callback for websocket in case connected to room notification is received
     * @param connectedToRoomCallback callback
     */
    void setConnectedToRoomCallback(IWebSocketCallback connectedToRoomCallback);
}
