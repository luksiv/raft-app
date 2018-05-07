package com.latenightpenguin.groupdj.Services;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.latenightpenguin.groupdj.ErrorHandler;
import com.latenightpenguin.groupdj.NetworkServices.ServerAPI.IServerHelper;
import com.latenightpenguin.groupdj.NetworkServices.ServerAPI.Requests.IRequestCallback;
import com.latenightpenguin.groupdj.NetworkServices.ServerAPI.RoomInfo;
import com.latenightpenguin.groupdj.NetworkServices.ServerAPI.SongConverter;
import com.latenightpenguin.groupdj.NetworkServices.ServerAPI.WebSocketStatus;
import com.latenightpenguin.groupdj.NetworkServices.ServerAPI.WebSockets.IWebSocketCallback;
import com.latenightpenguin.groupdj.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RoomService {
    private IServerHelper mServerHelper;
    private Context mContext;
    private ArrayList<OnChangeSubscriber> subscribers;

    public static final String ROOM_UPDATED = "ROOM_UPDATED";
    public static final String SONG_UPDATED = "SONG_UPDATED";
    public static final String PLAYTIME_UPDATED = "PLAYTIME_UPDATED";
    public static final String STATUS_UPDATED = "STATUS_UPDATED";
    public static final String SONG_LIST_UPDATED = "SONG_LIST_UPDATED";
    public static final String PAST_SONGS_UPDATED = "PAST_SONGS_UPDATED";

    private RoomInfo mRoom;
    private String mSong;
    private long mPlayTime;
    private SongStatus status;
    private ArrayList<String> mSongs;
    private ArrayList<String> mPastSongs;
    private boolean voted;
    private int lastPlayedCount;

    public RoomService(Context context, IServerHelper serverHelper){
        mServerHelper = serverHelper;
        mContext = context;
        status = SongStatus.PAUSED;
        voted = false;
        lastPlayedCount = 5;
        mRoom = new RoomInfo();
        subscribers = new ArrayList<>();

        subscribe(new OnChangeSubscriber() {
            @Override
            public void callback(String[] changes) {
                for(int i = 0; i < changes.length; i++){
                    if(changes[i].equals(ROOM_UPDATED)){
                        mServerHelper.setRoomUpdates(mRoom.getId());
                    }
                }
            }
        });
    }

    public ArrayList<String> getSongs(){
        return mSongs;
    }

    public RoomInfo getRoom(){
        return mRoom;
    }

    public String getCurrent(){
        return mSong;
    }

    public long getPlayTime(){
        return mPlayTime;
    }

    public SongStatus getStatus() {
        return status;
    }

    public ArrayList<String> getPastSongs() {
        return mPastSongs;
    }

    public IServerHelper getServerHelper() {
        return mServerHelper;
    }

    public void subscribe(OnChangeSubscriber subscriber){
        subscribers.add(subscriber);
    }

    public void notifyDataChanged(String ... changes){
        for(OnChangeSubscriber subscriber : subscribers){
            subscriber.callback(changes);
        }
    }

    public void refreshSongList(){
        mServerHelper.getSongs(mRoom, new IRequestCallback() {
            @Override
            public void onSuccess(String response) {
                if(response.equals("[]")){
                    mSongs = new ArrayList<>();
                } else {
                    mSongs = SongConverter.convertToList(response);
                }
                notifyDataChanged(SONG_LIST_UPDATED);
            }

            @Override
            public void onError(int code, String message) {
                handleError(code, message);
            }
        });
    }

    public void refreshCurrentSong(){
        mServerHelper.getCurrentSong(mRoom, new IRequestCallback() {
            @Override
            public void onSuccess(String response) {
                mSong = SongConverter.getSongId(response);
                notifyDataChanged(SONG_UPDATED);
            }

            @Override
            public void onError(int code, String message) {
                handleError(code, message);
            }
        });
    }

    public void refreshCurrentSong(String json){
        mSong = SongConverter.getSongId(json);
        notifyDataChanged(SONG_UPDATED);
    }

    public void refreshLastPlayedSongs(){
        mServerHelper.getLastPlayedSongs(mRoom, lastPlayedCount, new IRequestCallback() {
            @Override
            public void onSuccess(String response) {
                mPastSongs = SongConverter.convertToList(response);
                notifyDataChanged(PAST_SONGS_UPDATED);
            }

            @Override
            public void onError(int code, String message) {
                handleError(code, message);
            }
        });
    }

    public void createRoom(final String user){
        mServerHelper.registerUser(user, new IRequestCallback() {
            @Override
            public void onSuccess(String response) {
                mServerHelper.createRoom(user, new IRequestCallback() {
                    @Override
                    public void onSuccess(String response) {
                        parseRoomInfo(response);
                    }

                    @Override
                    public void onError(int code, String message) {
                        if(code == -1){
                            Toast.makeText(mContext, "Device is offline", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mContext, "Error creating room", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onError(int code, String message) {
                handleError(code, message);
            }
        });
    }

    public void connectToRoom(final String user, final int logincode){
        mServerHelper.registerUser(user, new IRequestCallback() {
            @Override
            public void onSuccess(String response) {
                mServerHelper.connectToRoom(user, logincode, new IRequestCallback() {
                    @Override
                    public void onSuccess(String response) {
                        parseRoomInfo(response);
                    }

                    @Override
                    public void onError(int code, String message) {
                        handleError(code, message);
                    }
                });
            }

            @Override
            public void onError(int code, String message) {
                handleError(code, message);
            }
        });
    }

    public void disconnect(String user){
        mServerHelper.disconnectFromRoom(user, new IRequestCallback() {
            @Override
            public void onSuccess(String response) {
                Toast.makeText(mContext, "Disconnected successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(int code, String message) {
                handleError(code, message);
            }
        });
    }

    public void addSong(String song){
        mServerHelper.addSong(mRoom, song, new IRequestCallback() {
            @Override
            public void onSuccess(String response) {
                Toast.makeText(mContext, "Song added", Toast.LENGTH_SHORT).show();
                refreshSongList();
            }

            @Override
            public void onError(int code, String message) {
                handleError(code, message);
            }
        });
    }

    public void voteSkipSong(){
        if(voted){
            Toast.makeText(mContext, "Already voted", Toast.LENGTH_SHORT).show();
            return;
        }

        voted = true;
        mServerHelper.voteSkipSong(mRoom, new IRequestCallback() {
            @Override
            public void onSuccess(String response) {
                Toast.makeText(mContext, "Voted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(int code, String message) {
                voted = false;
                handleError(code, message);
            }
        });
    }

    public void setSkipThreshold(double threshold){
        mServerHelper.setSkipThreshold(mRoom, threshold, new IRequestCallback() {
            @Override
            public void onSuccess(String response) {
                Toast.makeText(mContext, "Skip setting updated", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(int code, String message) {
                handleError(code, message);
            }
        });
    }

    public void playNextSong(String optionalSong){
        if(optionalSong == null){
            mServerHelper.playNextSong(mRoom, new IRequestCallback() {
                @Override
                public void onSuccess(String response) {
                    voted = false;
                    refreshLastPlayedSongs();
                    refreshSongList();
                    refreshCurrentSong(response);
                }

                @Override
                public void onError(int code, String message) {
                    handleError(code, message);
                }
            });
        } else {
            mServerHelper.playNext(mRoom, optionalSong, new IRequestCallback() {
                @Override
                public void onSuccess(String response) {
                    voted = false;
                    refreshLastPlayedSongs();
                    refreshSongList();
                    refreshCurrentSong(response);
                }

                @Override
                public void onError(int code, String message) {
                    handleError(code, message);
                }
            });
        }
    }

    public void announcePlayTime(long milliseconds){
        mServerHelper.announcePlayTime(milliseconds);
    }

    public void announcePause(long milliseconds){
        mServerHelper.announcePause(milliseconds);
    }

    public void ensureWebSocketIsConnected(){
        if(mServerHelper != null && mServerHelper.getWebSocketStatus() == WebSocketStatus.DISCONNECTED){
            if(mRoom.getId() == -1){
                Toast.makeText(mContext, "Not connected to room", Toast.LENGTH_SHORT).show();
            } else {
                mServerHelper.connectWebSocket();
                mServerHelper.setRoomUpdates(mRoom.getId());
            }
        }
    }

    private void setUpWebSocketCallbacks(){
        mServerHelper.setConnectedToRoomCallback(new IWebSocketCallback() {
            @Override
            public void execute(String message) {
                Toast.makeText(mContext, "Connected to room", Toast.LENGTH_SHORT).show();
            }
        });

        mServerHelper.setPlayingNextCallback(new IWebSocketCallback() {
            @Override
            public void execute(String message) {
                refreshCurrentSong();
                refreshLastPlayedSongs();
                refreshSongList();
            }
        });

        mServerHelper.setSongAddedCallback(new IWebSocketCallback() {
            @Override
            public void execute(String message) {
                refreshSongList();
            }
        });

        mServerHelper.setSongPausedCallback(new IWebSocketCallback() {
            @Override
            public void execute(String message) {
                if(status != SongStatus.PAUSED) {
                    status = SongStatus.PAUSED;
                    mPlayTime = Long.getLong(message);
                    notifyDataChanged(PLAYTIME_UPDATED, STATUS_UPDATED);
                } else {
                    mPlayTime = Long.getLong(message);
                    notifyDataChanged(PLAYTIME_UPDATED);
                }
            }
        });

        mServerHelper.setSongPlayTimeCallback(new IWebSocketCallback() {
            @Override
            public void execute(String message) {
                if(status != SongStatus.PLAYING) {
                    status = SongStatus.PLAYING;
                    mPlayTime = Long.getLong(message);
                    notifyDataChanged(PLAYTIME_UPDATED, STATUS_UPDATED);
                } else {
                    mPlayTime = Long.getLong(message);
                    notifyDataChanged(PLAYTIME_UPDATED);
                }
            }
        });

        mServerHelper.setSongSkippedCallback(new IWebSocketCallback() {
            @Override
            public void execute(String message) {
                refreshSongList();
                refreshLastPlayedSongs();
                refreshCurrentSong();
            }
        });
    }

    private void parseRoomInfo(String json){
        if (json != null) {
            try {
                JSONObject roomInfo = new JSONObject(json.toString());
                int roomId = roomInfo.getInt("id");
                int loginCode = roomInfo.getInt("logincode");

                mRoom.setId(roomId);
                mRoom.setLoginCode(loginCode);

                notifyDataChanged(ROOM_UPDATED);
                refreshCurrentSong();
                refreshSongList();
                refreshLastPlayedSongs();
            } catch (JSONException e) {
                Log.d("MusicDJ", json);
                ErrorHandler.handleExeption(e);
            }
        }
    }

    private void handleError(int code, String message){
        Log.d("RoomService", String.format("code: %d, message: %s", code, message));
        if(code == -1){
            Toast.makeText(mContext, mContext.getString(R.string.offline), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
        }
    }

    public interface OnChangeSubscriber{
        void callback(String[] changes);
    }

    public enum SongStatus{
        PLAYING,
        PAUSED
    }
}