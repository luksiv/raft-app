package com.latenightpenguin.groupdj.Services;

import android.content.Context;
import android.util.Log;

import com.latenightpenguin.groupdj.ErrorHandler;
import com.latenightpenguin.groupdj.NetworkServices.ServerAPI.IServerHelper;
import com.latenightpenguin.groupdj.NetworkServices.ServerAPI.Requests.IRequestCallback;
import com.latenightpenguin.groupdj.NetworkServices.ServerAPI.RoomInfo;
import com.latenightpenguin.groupdj.NetworkServices.ServerAPI.SongConverter;
import com.latenightpenguin.groupdj.NetworkServices.ServerAPI.WebSocketStatus;
import com.latenightpenguin.groupdj.NetworkServices.ServerAPI.WebSockets.IWebSocketCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RoomService {
    private final String TAG = "RoomService";
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
    private boolean isHost;

    public RoomService(Context context, IServerHelper serverHelper){
        mServerHelper = serverHelper;
        mContext = context;
        status = SongStatus.PAUSED;
        voted = false;
        lastPlayedCount = 5;
        mRoom = new RoomInfo();
        subscribers = new ArrayList<>();
        isHost = false;
        setUpWebSocketCallbacks();

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

    public int getSongCount(){
        return mSongs.size();
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
                Log.d(TAG, SONG_LIST_UPDATED);
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
                Log.d(TAG, SONG_UPDATED);
                notifyDataChanged(SONG_UPDATED);
            }

            @Override
            public void onError(int code, String message) {
                handleError(code, message);
            }
        });
    }

    private void refreshCurrentSong(String json){
        mSong = SongConverter.getSongId(json);
        Log.d(TAG, SONG_UPDATED);
        notifyDataChanged(SONG_UPDATED);
    }

    public void refreshLastPlayedSongs(){
        mServerHelper.getLastPlayedSongs(mRoom, lastPlayedCount, new IRequestCallback() {
            @Override
            public void onSuccess(String response) {
                mPastSongs = SongConverter.convertToList(response);
                Log.d(TAG, PAST_SONGS_UPDATED);
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
                        mServerHelper.connectWebSocket();
                        mServerHelper.setRoomUpdates(mRoom.getId());
                        isHost = true;
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

    public void connectToRoom(final String user, final int logincode){
        mServerHelper.registerUser(user, new IRequestCallback() {
            @Override
            public void onSuccess(String response) {
                mServerHelper.connectToRoom(user, logincode, new IRequestCallback() {
                    @Override
                    public void onSuccess(String response) {
                        parseRoomInfo(response);
                        mServerHelper.connectWebSocket();
                        mServerHelper.setRoomUpdates(mRoom.getId());
                        isHost = false;
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
        mServerHelper.closeWebSocket();
        mServerHelper.disconnectFromRoom(mRoom.getId(), user, new IRequestCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "Disconnected successfully");
                //Toast.makeText(mContext, "Disconnected successfully", Toast.LENGTH_SHORT).show();
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
                Log.d(TAG, "Song added");
                //Toast.makeText(mContext, "Song added", Toast.LENGTH_SHORT).show();
                refreshSongList();
                refreshLastPlayedSongs();
                if(mSong == null || mSong.isEmpty()){
                    refreshCurrentSong();
                }
            }

            @Override
            public void onError(int code, String message) {
                handleError(code, message);
            }
        });
    }

    public void voteSkipSong(){
        if(voted){
            Log.d(TAG, "Already voted");
            //Toast.makeText(mContext, "Already voted", Toast.LENGTH_SHORT).show();
            return;
        }

        voted = true;
        mServerHelper.voteSkipSong(mRoom, new IRequestCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "Voted");
                //Toast.makeText(mContext, "Voted", Toast.LENGTH_SHORT).show();
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
                Log.d(TAG, "Skip setting updated");
                //Toast.makeText(mContext, "Skip setting updated", Toast.LENGTH_SHORT).show();
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

                    mSong = SongConverter.getSongId(response);
                    Log.d(TAG, SONG_UPDATED);
                    notifyDataChanged(SONG_UPDATED);

                    refreshLastPlayedSongs();
                    refreshSongList();
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

                    mSong = SongConverter.getSongId(response);
                    Log.d(TAG, SONG_UPDATED);
                    notifyDataChanged(SONG_UPDATED);

                    refreshLastPlayedSongs();
                    refreshSongList();
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
                Log.d(TAG, "Not connected to room");
                //Toast.makeText(mContext, "Not connected to room", Toast.LENGTH_SHORT).show();
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
                Log.d(TAG, "connectedcallback");//veikia
                Log.d(TAG, "Connected to room");
                //Toast.makeText(mContext, "Connected to room", Toast.LENGTH_SHORT).show();
            }
        });

        mServerHelper.setPlayingNextCallback(new IWebSocketCallback() {
            @Override
            public void execute(String message) {
                Log.d(TAG, "nextcallback");//veikia
                refreshCurrentSong();
                refreshLastPlayedSongs();
                refreshSongList();
            }
        });

        mServerHelper.setSongAddedCallback(new IWebSocketCallback() {
            @Override
            public void execute(String message) {
                Log.d(TAG, "addedcallback");//veikia
                refreshSongList();
            }
        });

        mServerHelper.setSongPausedCallback(new IWebSocketCallback() {
            @Override
            public void execute(String message) {
                Log.d(TAG, "pausedcallback");//veikia
                if(status != SongStatus.PAUSED) {
                    status = SongStatus.PAUSED;
                    mPlayTime = Long.parseLong(message);
                    Log.d(TAG, PLAYTIME_UPDATED + " " + STATUS_UPDATED);
                    notifyDataChanged(PLAYTIME_UPDATED, STATUS_UPDATED);
                } else {
                    mPlayTime = Long.parseLong(message);
                    Log.d(TAG, PLAYTIME_UPDATED);
                    notifyDataChanged(PLAYTIME_UPDATED);
                }
            }
        });

        mServerHelper.setSongPlayTimeCallback(new IWebSocketCallback() {
            @Override
            public void execute(String message) {
                Log.d(TAG, "playtimecallback");//veikia
                if(status != SongStatus.PLAYING) {
                    status = SongStatus.PLAYING;
                    Log.d(TAG, "message:" + message);
                    mPlayTime = Long.parseLong(message);
                    Log.d(TAG, PLAYTIME_UPDATED + " " + STATUS_UPDATED);
                    notifyDataChanged(PLAYTIME_UPDATED, STATUS_UPDATED);
                } else {
                    mPlayTime = Long.parseLong(message);
                    Log.d(TAG, PLAYTIME_UPDATED);
                    notifyDataChanged(PLAYTIME_UPDATED);
                }
            }
        });

        mServerHelper.setSongSkippedCallback(new IWebSocketCallback() {
            @Override
            public void execute(String message) {
                Log.d(TAG, "skipcallback");//neveikia
                if(isHost) {
                    playNextSong(null);
                }
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

                Log.d(TAG, ROOM_UPDATED);
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
            Log.d(TAG, "Device is offline");
            //Toast.makeText(mContext, mContext.getString(R.string.offline), Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, message);
            //Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
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
