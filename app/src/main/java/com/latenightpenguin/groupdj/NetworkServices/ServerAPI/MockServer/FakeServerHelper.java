package com.latenightpenguin.groupdj.NetworkServices.ServerAPI.MockServer;

import android.util.Log;

import com.latenightpenguin.groupdj.NetworkServices.ServerAPI.Requests.IRequestCallback;
import com.latenightpenguin.groupdj.NetworkServices.ServerAPI.IServerHelper;
import com.latenightpenguin.groupdj.NetworkServices.ServerAPI.WebSocketStatus;
import com.latenightpenguin.groupdj.NetworkServices.ServerAPI.RoomInfo;
import com.latenightpenguin.groupdj.NetworkServices.ServerAPI.WebSockets.IWebSocketCallback;

import java.util.ArrayList;
import java.util.Random;

public class FakeServerHelper implements IServerHelper {
    private static final String CLASS_TAG = "FakeServer";
    private String mUser;
    private FakeRoom mRoom;
    private ArrayList<FakeSong> mSongList;
    WebSocketStatus socketStatus = WebSocketStatus.DISCONNECTED;

    private IWebSocketCallback playingNextCallback;
    private IWebSocketCallback songAddedCallback;
    private IWebSocketCallback songPausedCallback;
    private IWebSocketCallback songPlayTimeCallback;
    private IWebSocketCallback songSkippedCallback;
    private IWebSocketCallback connectedToRoomCallback;

    private String[] songs = {
            "spotify:track:32zmm5WQw5B5JJ9lMc5nNt",
            "spotify:track:2AT8iROs4FQueDv2c8q2KE",
            "spotify:track:7HwQmdn5arIsVfktUzLQfd",
            "spotify:track:4wVyHA4jjPwfEzLwbIUgMc",
            "spotify:track:5hheGdf1cb4rK0FNiedCfK"
    };

    @Override
    public void registerUser(String user, IRequestCallback callback) {
        if (user != null && !user.equals("")){
            mUser = user;
            callback.onSuccess("");
            return;
        }
        callback.onError(400, "Bad request");
    }

    @Override
    public void createRoom(String user, IRequestCallback callback) {
        if(user == null || user.equals("")){
            callback.onError(400,"Bad request");
            return;
        }

        if(mUser.equals(user)){
            mRoom = new FakeRoom(200, 12345, 10);
            mSongList = new ArrayList<>();

            callback.onSuccess("{\"id\":200,\"logincode\":12345}");
            return;
        }
        callback.onError(404,"User or room does not exist");
    }

    @Override
    public void connectToRoom(String user, int loginCode, IRequestCallback callback) {
        if(user != null && !user.equals("")){
            if(mUser.equals(user) && loginCode == 12345){
                mRoom = new FakeRoom(200, 12345, 10);
                mSongList = new ArrayList<>();

                callback.onSuccess("{\"id\":200,\"logincode\":12345}");
                return;
            }

            callback.onError(404,"User or room does not exist");
            return;
        }

        callback.onError(400,"Bad request");
    }

    @Override
    public void disconnectFromRoom(String user, IRequestCallback callback) {
        if(user != null && !user.equals("")){
            if(!user.equals(mUser)){
                callback.onError(404,"user does not exist");
                return;
            }

            if(mRoom == null){
                callback.onError(400,"user is not in room");
                return;
            }

            mRoom = null;
            mSongList = null;
            callback.onSuccess("");

            return;
        }
        callback.onError(400,"Bad request");
    }

    @Override
    public void addSong(RoomInfo room, String song, IRequestCallback callback) {
        if(room.getId() != -1 && song != null && !song.equals("")){
            if(mRoom == null){
                callback.onError(404,"Error finding room");
                return;
            }

            mSongList.add(new FakeSong(song, mSongList.size() + 1));
            callback.onSuccess("");
            if(songAddedCallback != null && socketStatus == WebSocketStatus.CONNECTED){
                songAddedCallback.execute("");
            }
            return;
        }
        callback.onError(400,"Bad request");
    }

    @Override
    public void getCurrentSong(RoomInfo room, IRequestCallback callback) {
        if(room.getId() != -1){
            if(mRoom == null){
                callback.onError(404,"Song could not be found");
                return;
            }

            if(mSongList.size() == 0){
                callback.onSuccess("");
                return;
            }

            int index = mRoom.getSongIndex() - 1 >= 0 ? mRoom.getSongIndex() - 1 : 0;
            FakeSong song = mSongList.get(index);
            callback.onSuccess(String.format("{\"song\":\"%s\",\"queuepos\":%d}", song.getId(), song.getPos()));
            return;
        }
        Log.w(CLASS_TAG, "Room is not set");
        callback.onError(400,"Bad request");
    }

    @Override
    public void getSongs(RoomInfo room, IRequestCallback callback) {
        if(room.getId() != -1){
            if(mRoom == null){
                callback.onError(404, "Song list could not be found");
                return;
            }

            StringBuilder json = new StringBuilder();
            json.append("[");

            if(mSongList.size() > 0) {
                int start = mRoom.getSongIndex() - 2 >= 0 ? mRoom.getSongIndex() - 2 : 0;
                for (int i = start; i < mSongList.size(); i++) {
                    json.append(String.format("{\"song\":\"%s\",\"queuepos\":%d}", mSongList.get(i).getId(), mSongList.get(i).getPos()));

                    if (mSongList.size() > i + 1) {
                        json.append(",");
                    }
                }
            }

            json.append("]");

            callback.onSuccess(json.toString());
            return;
        }
        Log.w(CLASS_TAG, "Room is not set");
        callback.onError(400, "Bad request");
    }

    @Override
    public void playNext(RoomInfo room, String song, IRequestCallback callback) {
        if(room.getId() != -1 && song != null && !song.equals("")){
            if(mRoom == null){
                callback.onError(404,"Error finding room");
                return;
            }
            if(mRoom.getSongIndex() + 1 == mSongList.size()){
                mSongList.add(new FakeSong(song, mSongList.size() + 1));
            }

            FakeSong fakeSong = mSongList.get(mRoom.getSongIndex() - 1);
            mRoom.setSongIndex(mRoom.getSongIndex() + 1);
            mRoom.setVoteOut(0);
            callback.onSuccess(String.format("{\"song\":\"%s\",\"queuepos\":%d}", fakeSong.getId(), fakeSong.getPos()));
            if(playingNextCallback != null && socketStatus == WebSocketStatus.CONNECTED){
                playingNextCallback.execute("");
            }
            Log.d(CLASS_TAG, "playing next");
            return;
        }
        callback.onError(400,"Bad request");
    }

    @Override
    public void playNextSong(RoomInfo room, IRequestCallback callback) {
        if(room.getId() != -1){
            if(mRoom == null || mRoom.getSongIndex() + 1 < mSongList.size()){
                callback.onError(404, "Error finding next song");
                return;
            }

            FakeSong song = mSongList.get(mRoom.getSongIndex() - 1);
            mRoom.setSongIndex(mRoom.getSongIndex() + 1);
            mRoom.setVoteOut(0);
            callback.onSuccess(String.format("{\"song\":\"%s\",\"queuepos\":%d}", song.getId(), song.getPos()));
            if(playingNextCallback != null && socketStatus == WebSocketStatus.CONNECTED){
                playingNextCallback.execute("");
            }
            Log.d(CLASS_TAG, "playing next");
            return;
        }
        callback.onError(400,"Bad request");
    }

    @Override
    public void getLastPlayedSongs(RoomInfo room, int count, IRequestCallback callback) {
        if(room.getId() != -1 && count > 0){
            if(mRoom == null){
                callback.onError(404,"Song list not be found");
                return;
            }

            StringBuilder json = new StringBuilder();
            json.append("[");

            for(int i = 0; i < count; i++){
                int index = i + mRoom.getSongIndex() - 1 - count;
                if(index >= 0) {
                    json.append(String.format("{\"song\":\"%s\",\"queuepos\":%d}", mSongList.get(index).getId(), mSongList.get(index).getPos()));

                    if(i + 1 < count){
                        json.append(",");
                    }
                }
            }

            json.append("]");

            callback.onSuccess(json.toString());
            return;
        }
        Log.w(CLASS_TAG, "Room is not set");
        callback.onError(400,"Bad request");
    }

    @Override
    public void getLeftSongCount(RoomInfo room, IRequestCallback callback) {
        if(room.getId() != -1){
            if(mRoom == null){
                callback.onSuccess("0");
                return;
            }

            int count = mSongList.size() - mRoom.getSongIndex() + 1;
            callback.onSuccess(Integer.toString(count >= 0 ? count : 0));
            return;
        }
        Log.w(CLASS_TAG, "Room is not set");
        callback.onError(400,"Bad request");
    }

    @Override
    public void voteSkipSong(RoomInfo room, IRequestCallback callback) {
        if(mRoom == null){
            Log.w(CLASS_TAG, "Not connected to room");
            return;
        }
        if(room.getId() != -1){
            if(room.getId() != mRoom.getId()){
                callback.onError(404,"room not found");
                return;
            }

            mRoom.setVoteOut(mRoom.getVoteOut() + 1);

            if(Double.compare(mRoom.getVoteOut() / 10.0, mRoom.getThreshHold()) > 0){
                if(socketStatus == WebSocketStatus.CONNECTED && songSkippedCallback != null){
                    songSkippedCallback.execute("");
                }
            }

            callback.onSuccess("");
            return;
        }

        Log.w(CLASS_TAG, "Room is not set");
        callback.onError(400,"Bad request");
    }

    @Override
    public void setSkipThreshold(RoomInfo room, double threshold, IRequestCallback callback) {
        if(mRoom == null){
            Log.w(CLASS_TAG, "Not connected to room");
            return;
        }
        if(room.getId() != -1){
            if(room.getId() != mRoom.getId()){
                callback.onError(404,"room does not found");
                return;
            }

            mRoom.setThreshHold(threshold);

            callback.onSuccess("");
            return;
        }

        Log.w(CLASS_TAG, "Room is not set");
        callback.onError(400,"Bad request");
    }

    @Override
    public void waitForAllRequests() {}

    @Override
    public void connectWebSocket() {
        socketStatus = WebSocketStatus.CONNECTED;
    }

    @Override
    public void closeWebSocket() {
        socketStatus = WebSocketStatus.DISCONNECTED;
    }

    @Override
    public WebSocketStatus getWebSocketStatus() {
        return socketStatus;
    }

    @Override
    public void setRoomUpdates(int id) {
        if(mRoom == null){
            Log.w(CLASS_TAG, "Not connected to room");
            return;
        }
        if(id != mRoom.getId()){
            Log.w(CLASS_TAG, String.format("Websocket room id set to %d when it should be %d", id, mRoom.getId()));
        }

        if(socketStatus == WebSocketStatus.CONNECTED && connectedToRoomCallback != null) {
            connectedToRoomCallback.execute(String.format("room set to %d", id));
        }
    }

    @Override
    public void announcePlayTime(long milliseconds) {
        if(socketStatus == WebSocketStatus.CONNECTED && connectedToRoomCallback != null) {
            songPlayTimeCallback.execute(Long.toString(milliseconds));
        }
    }

    @Override
    public void announcePause(long milliseconds) {
        if(socketStatus == WebSocketStatus.CONNECTED && connectedToRoomCallback != null) {
            songPausedCallback.execute(Long.toString(milliseconds));
        }
    }

    @Override
    public void setPlayingNextCallback(IWebSocketCallback playingNextCallback) {
        this.playingNextCallback = playingNextCallback;
    }

    @Override
    public void setSongAddedCallback(IWebSocketCallback songAddedCallback) {
        this.songAddedCallback = songAddedCallback;
    }

    @Override
    public void setSongPausedCallback(IWebSocketCallback songPausedCallback) {
        this.songPausedCallback = songPausedCallback;
    }

    @Override
    public void setSongPlayTimeCallback(IWebSocketCallback songPlayTimeCallback) {
        this.songPlayTimeCallback = songPlayTimeCallback;
    }

    @Override
    public void setSongSkippedCallback(IWebSocketCallback songSkippedCallback) {
        this.songSkippedCallback = songSkippedCallback;
    }

    @Override
    public void setConnectedToRoomCallback(IWebSocketCallback connectedToRoomCallback) {
        this.connectedToRoomCallback = connectedToRoomCallback;
    }

    public void voteAsSomeoneElse(){
        if(mRoom == null){
            return;
        }

        mRoom.setVoteOut(mRoom.getVoteOut() + 1);

        if(Double.compare(mRoom.getVoteOut() / 10.0, mRoom.getThreshHold()) > 0){
            if(socketStatus == WebSocketStatus.CONNECTED && songSkippedCallback != null){
                songSkippedCallback.execute("");
            }
        }
    }

    public void addAsSomeoneElse(){
        if(mRoom == null){
            return;
        }

        Random rand = new Random();
        String song = songs[rand.nextInt(songs.length)];

        mSongList.add(new FakeSong(song, mSongList.size() + 1));
        if(songAddedCallback != null && socketStatus == WebSocketStatus.CONNECTED){
            songAddedCallback.execute("");
        }
        return;
    }

    public void playNextAsSomeoneElse(){
        if(mRoom == null){
            return;
        }

        FakeSong song = mSongList.get(mRoom.getSongIndex() - 1);
        mRoom.setSongIndex(mRoom.getSongIndex() + 1);
        mRoom.setVoteOut(0);
        if(playingNextCallback != null && socketStatus == WebSocketStatus.CONNECTED){
            playingNextCallback.execute("");
        }
    }

    public void pauseAsSomeoneElse(long milliseconds){
        if(mRoom == null){
            return;
        }

        if(socketStatus == WebSocketStatus.CONNECTED && songPausedCallback != null) {
            songPausedCallback.execute(Long.toString(milliseconds));
        }
    }

    public void playTimeAsSomeoneElse(long milliseconds){
        if(mRoom == null){
            return;
        }

        if(socketStatus == WebSocketStatus.CONNECTED && songPlayTimeCallback != null) {
            songPlayTimeCallback.execute(Long.toString(milliseconds));
        }
    }
}
