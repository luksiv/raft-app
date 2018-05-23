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
    private boolean updated;
    private boolean skip;

    public boolean connected = false;
    public boolean connectedToRoom = false;
    public boolean done = false;

    public RoomService(IServerHelper serverHelper) {
        mServerHelper = serverHelper;
        status = SongStatus.PAUSED;
        voted = false;
        lastPlayedCount = 5;
        mRoom = new RoomInfo();
        subscribers = new ArrayList<>();
        isHost = false;
        mSong = "";
        skip = false;
        setUpWebSocketCallbacks();

        subscribe(new OnChangeSubscriber() {
            @Override
            public void callback(String[] changes) {
                for (int i = 0; i < changes.length; i++) {
                    if (changes[i].equals(ROOM_UPDATED)) {
                        mServerHelper.setRoomUpdates(mRoom.getId());
                    }
                }
            }
        });
    }

    public ArrayList<String> getSongs() {
        return mSongs;
    }

    public int getSongCount() {
        return mSongs.size();
    }

    public RoomInfo getRoom() {
        return mRoom;
    }

    public String getCurrent() {
        return mSong;
    }

    public long getPlayTime() {
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

    public boolean isSkipped() {
        boolean skipValue = skip;
        skip = false;
        return skipValue;
    }

    public void subscribe(OnChangeSubscriber subscriber) {
        subscribers.add(subscriber);
    }

    public void notifyDataChanged(String... changes) {
        for (OnChangeSubscriber subscriber : subscribers) {
            subscriber.callback(changes);
        }
    }

    public void refreshSongList() {
        mServerHelper.getSongs(mRoom, new IRequestCallback() {
            @Override
            public void onSuccess(String response) {
                if (response.equals("[]")) {
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

    public void refreshCurrentSong() {
        mServerHelper.getCurrentSong(mRoom, new IRequestCallback() {
            @Override
            public void onSuccess(String response) {
                String song = SongConverter.getSongId(response);
                updateSong(song);
            }

            @Override
            public void onError(int code, String message) {
                handleError(code, message);
            }
        });
    }

    private void refreshCurrentSong(String json){
        String song = SongConverter.getSongId(json);
        updateSong(song);
    }

    public void refreshLastPlayedSongs() {
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

    public void createRoom(final String user) {
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

    public void connectToRoom(final String user, final int logincode) {
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
                        connectedToRoom = true;
                        done = true;
                    }

                    @Override
                    public void onError(int code, String message) {
                        handleError(code, message);
                        connectedToRoom = false;
                        done = true;
                    }
                });
            }

            @Override
            public void onError(int code, String message) {
                handleError(code, message);
                connectedToRoom = false;
                done = true;
            }
        });
    }

    public void disconnect(String user) {
        mServerHelper.closeWebSocket();
        mServerHelper.disconnectFromRoom(mRoom.getId(), user, new IRequestCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "Disconnected successfully");
            }

            @Override
            public void onError(int code, String message) {
                handleError(code, message);
            }
        });
    }

    public void addSong(String song) {
        mServerHelper.addSong(mRoom, song, new IRequestCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "Song added");
                refreshSongList();
                refreshLastPlayedSongs();
                if (mSong == null || mSong.isEmpty()) {
                    refreshCurrentSong();
                }
            }

            @Override
            public void onError(int code, String message) {
                handleError(code, message);
            }
        });
    }

    public void voteSkipSong() {
        Log.d(TAG, "voteSkipSong: " + skip);
        if (voted) {
            Log.d(TAG, "Already voted");
            return;
        }

        voted = true;
        mServerHelper.voteSkipSong(mRoom, new IRequestCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "Voted");
            }

            @Override
            public void onError(int code, String message) {
                voted = false;
                handleError(code, message);
            }
        });
    }

    public void setSkipThreshold(double threshold) {
        mServerHelper.setSkipThreshold(mRoom, threshold, new IRequestCallback() {
            @Override
            public void onSuccess(String response) {
            }

            @Override
            public void onError(int code, String message) {
                handleError(code, message);
            }
        });
    }

    public void playNextSong(String optionalSong){
        Log.d(TAG, "playNextSong: " + updated);
        if(!updated) {
            return;
        }

        if(optionalSong == null){
            mServerHelper.playNextSong(mRoom, new IRequestCallback() {
                @Override
                public void onSuccess(String response) {
                    voted = false;

                    String song = SongConverter.getSongId(response);
                    updateSong(song);

                    refreshLastPlayedSongs();
                    refreshSongList();
                }

                @Override
                public void onError(int code, String message) {
                    handleError(code, message);
                }
            });
            updated = false;
        } else {
            mServerHelper.playNext(mRoom, optionalSong, new IRequestCallback() {
                @Override
                public void onSuccess(String response) {
                    voted = false;

                    String song = SongConverter.getSongId(response);
                    updateSong(song);

                    refreshLastPlayedSongs();
                    refreshSongList();
                }

                @Override
                public void onError(int code, String message) {
                    handleError(code, message);
                }
            });
            updated = false;
        }
    }

    public void announcePlayTime(long milliseconds) {
        mServerHelper.announcePlayTime(milliseconds);
    }

    public void announcePause(long milliseconds) {
        mServerHelper.announcePause(milliseconds);
    }

    public void ensureWebSocketIsConnected() {
        if (mServerHelper != null && mServerHelper.getWebSocketStatus() == WebSocketStatus.DISCONNECTED) {
            if (mRoom.getId() == -1) {
                Log.d(TAG, "Not connected to room");
            } else {
                mServerHelper.connectWebSocket();
                mServerHelper.setRoomUpdates(mRoom.getId());
            }
        }
    }

    private void setUpWebSocketCallbacks() {
        mServerHelper.setConnectedToRoomCallback(new IWebSocketCallback() {
            @Override
            public void execute(String message) {
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
                if (status != SongStatus.PAUSED) {
                    status = SongStatus.PAUSED;
                    mPlayTime = Long.parseLong(message);
                    notifyDataChanged(PLAYTIME_UPDATED, STATUS_UPDATED);
                } else {
                    mPlayTime = Long.parseLong(message);
                    notifyDataChanged(PLAYTIME_UPDATED);
                }
            }
        });

        mServerHelper.setSongPlayTimeCallback(new IWebSocketCallback() {
            @Override
            public void execute(String message) {
                if (status != SongStatus.PLAYING) {
                    status = SongStatus.PLAYING;
                    mPlayTime = Long.parseLong(message);
                    notifyDataChanged(PLAYTIME_UPDATED, STATUS_UPDATED);
                } else {
                    mPlayTime = Long.parseLong(message);
                    notifyDataChanged(PLAYTIME_UPDATED);
                }
            }
        });

        mServerHelper.setSongSkippedCallback(new IWebSocketCallback() {
            @Override
            public void execute(String message) {
                if (isHost && !skip) {
                    playNextSong(null);
                    skip = true;
                }
            }
        });
    }

    private void parseRoomInfo(String json) {
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

    private void handleError(int code, String message) {
        Log.d("RoomService", String.format("code: %d, message: %s", code, message));
        if (code == -1) {
            Log.d(TAG, "Device is offline");
        } else {
            Log.d(TAG, message);
        }
    }

    private void updateSong(String newSong) {
        if(!mSong.equals(newSong)) {
            mSong = newSong;
            Log.d(TAG, SONG_UPDATED);
            notifyDataChanged(SONG_UPDATED);
            updated = true;
        }
    }

    public interface OnChangeSubscriber{
        void callback(String[] changes);
    }

    public enum SongStatus {
        PLAYING,
        PAUSED
    }
}
