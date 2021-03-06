package com.latenightpenguin.groupdj;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.latenightpenguin.groupdj.Models.SongItem;
import com.latenightpenguin.groupdj.Models.User;
import com.latenightpenguin.groupdj.NetworkServices.ServerAPI.IServerHelper;
import com.latenightpenguin.groupdj.NetworkServices.ServerAPI.ServerFactory;
import com.latenightpenguin.groupdj.NetworkServices.SpotifyAPI.SpotifyData;
import com.latenightpenguin.groupdj.NetworkServices.SpotifyAPI.WrappedSpotifyCallback;
import com.latenightpenguin.groupdj.Services.RoomService;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Connectivity;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Metadata;
import com.spotify.sdk.android.player.PlaybackBitrate;
import com.spotify.sdk.android.player.PlaybackState;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

import kaaes.spotify.webapi.android.models.Recommendations;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import kaaes.spotify.webapi.android.models.UserPrivate;

import static com.spotify.sdk.android.player.PlaybackBitrate.BITRATE_HIGH;
import static com.spotify.sdk.android.player.PlaybackBitrate.BITRATE_LOW;
import static com.spotify.sdk.android.player.PlaybackBitrate.BITRATE_NORMAL;

public class HostActivity extends AppCompatActivity implements
        SpotifyPlayer.NotificationCallback, ConnectionStateCallback {

    //region Constants
    private static final String TAG = "HostActivity";
    private static final String CLIENT_ID = "1b02f619aa8142db8cd6d3d9bc3d505e";
    private static final String REDIRECT_URI = "lnpapp://callback";
    private static final int AUTH_CODE = 1337;
    //endregion

    //region Fields
    private String mAccessToken;
    private PlaybackState mCurrentPlaybackState;
    private BroadcastReceiver mNetworkStateReceiver;
    private Metadata mMetadata;
    private final Player.OperationCallback mOperationCallback = new Player.OperationCallback() {
        @Override
        public void onSuccess() {
            Log.d("Callback", "OK!");
        }

        @Override
        public void onError(Error error) {
            Log.d("Callback", "ERROR:" + error);
        }
    };
    private Player mPlayer;
    private User mUser;
    private Handler mHandler = new Handler();
    private PlaylistArrayAdapter mPlaylistAdapter;
    private SpotifyData mSpotifyData;
    private RoomService mRoomService;

    private Boolean firstRun = true;
    private Boolean firstRunSkip = false;
    private Boolean queued = false;
    private boolean requestedNext = false;

    private String lastQueued = "";

    private int updateCount = 0;
    //endregion

    //region UI elements
    private Button btnAdd;
    private ImageButton btnPause;
    private ImageButton btnNext;
    private SeekBar sbTrack;
    private Button btnSettings;
    private Button btnInfo;
    private Button btnToggleViews;
    private Button btnRefreshPlaylist;
    private ListView lwPlaylist;
    private TextView txLoginCode;
    private Button testSkip;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        // ERROR HANDLER
        Thread.setDefaultUncaughtExceptionHandler(new ErrorHandler());
        ErrorHandler.setContext(HostActivity.this);
        ErrorHandler.setView(findViewById(R.id.root_hostactivity));

        setUpElements();
        authentication();

        IServerHelper serverHelper = ServerFactory.make(getResources().getString(R.string.url));
        mRoomService = new RoomService(serverHelper);
        setUpRoomChangeHandler();

        TracksRepository.setUp();

    }

    //region Methods that onCreate uses
    private void setUpElements() {
        txLoginCode = findViewById(R.id.tv_RoomId);
        btnAdd = findViewById(R.id.btn_AddSong);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HostActivity.this,
                        AddSongActivity.class);
                intent.putExtra("accessToken", mAccessToken);
                startActivityForResult(intent, 333);
            }
        });
        btnPause = findViewById(R.id.btn_playPause);
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentPlaybackState != null && mCurrentPlaybackState.isPlaying) {
                    mPlayer.pause(mOperationCallback);
                } else {
                    mPlayer.resume(mOperationCallback);
                }
            }
        });
        btnNext = findViewById(R.id.btn_next);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*if (mCurrentPlaybackState != null
                        && mMetadata != null && mMetadata.nextTrack != null) {
                    mPlayer.skipToNext(mOperationCallback);
                }*/
                mRoomService.voteSkipSong();
            }
        });
        btnToggleViews = findViewById(R.id.btn_toggleView);
        btnToggleViews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeViews(btnToggleViews);
            }
        });
        btnSettings = findViewById(R.id.btn_roomSettings);
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeBitRate();
            }
        });
        btnInfo = findViewById(R.id.btn_roomInfo);
        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInfoRoom();
            }
        });

        btnRefreshPlaylist = findViewById(R.id.btn_refreshPlaylist);
        btnRefreshPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRoomService.refreshSongList();
                mRoomService.refreshCurrentSong();
                mRoomService.refreshLastPlayedSongs();
                IServerHelper serverHelper = mRoomService.getServerHelper();
                ServerFactory.AdditionalCallbacks callbacks = ServerFactory.getAdditionalCallbacks(serverHelper);
                if (callbacks != null) {
                    callbacks.add();
                    callbacks.next();
                    callbacks.vote();
                    callbacks.pause(132);
                    callbacks.playtime(123);
                }
            }
        });

        sbTrack = findViewById(R.id.sb_seekTrack);
        sbTrack.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (mMetadata != null && mMetadata.currentTrack != null) {
                    double where = (double) i / 100;
                    double posx = mMetadata.currentTrack.durationMs * where;
                    int pos = (int) Math.round(posx);
                    updateTextView(R.id.tv_trackTime, Utilities.formatSeconds((long) pos));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mCurrentPlaybackState.isPlaying) mPlayer.pause(mOperationCallback);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mMetadata != null && mMetadata.currentTrack != null) {
                    double where = (double) seekBar.getProgress() / 100;
                    double posx = mMetadata.currentTrack.durationMs * where;
                    int pos = (int) Math.round(posx);
                    updateTextView(R.id.tv_trackTime, Utilities.formatSeconds((long) pos));
                    mPlayer.seekToPosition(mOperationCallback, pos);
                    mPlayer.resume(mOperationCallback);
                }
            }
        });

        lwPlaylist = findViewById(R.id.lw_playlist);
        mPlaylistAdapter = new PlaylistArrayAdapter(this, new ArrayList<SongItem>());
        lwPlaylist.setAdapter(mPlaylistAdapter);

        testSkip = findViewById(R.id.test_seek_to_end);
        testSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long dur = mPlayer.getMetadata().currentTrack.durationMs;
                int skip = (int) ((double) dur / 100 * 95);
                mPlayer.seekToPosition(mOperationCallback, skip);
            }
        });

        setVisibilityForUiElements(View.INVISIBLE);
        findViewById(R.id.root_player).setVisibility(View.INVISIBLE);
        findViewById(R.id.ll_start).setVisibility(View.INVISIBLE);
    }

    private void authentication() {
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "user-read-email", "streaming"});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, AUTH_CODE, request);
    }

    private void setVisibilityForUiElements(int visibility) {
        btnAdd.setVisibility(visibility);
        btnPause.setVisibility(visibility);
        btnNext.setVisibility(visibility);
        sbTrack.setVisibility(visibility);
        btnSettings.setVisibility(visibility);
        btnInfo.setVisibility(visibility);
        btnToggleViews.setVisibility(visibility);
        btnRefreshPlaylist.setVisibility(visibility);
        lwPlaylist.setVisibility(visibility);
        txLoginCode.setVisibility(visibility);
        testSkip.setVisibility(visibility);
    }
    //endregion

    //region Network
    @Override
    protected void onResume() {
        super.onResume();

        // Set up the broadcast receiver for network events. Note that we also unregister
        // this receiver again in onPause().
        mNetworkStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (mPlayer != null) {
                    Connectivity connectivity = getNetworkConnectivity(getBaseContext());
                    ErrorHandler.handleMessege("Network state changed: " + connectivity.toString());
                    mPlayer.setConnectivityStatus(mOperationCallback, connectivity);
                }
            }
        };

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetworkStateReceiver, filter);

        if (mPlayer != null) {
            mPlayer.addNotificationCallback(HostActivity.this);
            mPlayer.addConnectionStateCallback(HostActivity.this);
        }

        if (mRoomService != null) {
            mRoomService.ensureWebSocketIsConnected();
        }
    }

    /**
     * Registering for connectivity changes in Android does not actually deliver them to
     * us in the delivered intent.
     *
     * @param context Android context
     * @return Connectivity state to be passed to the SDK
     */
    private Connectivity getNetworkConnectivity(Context context) {
        ConnectivityManager connectivityManager;
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            return Connectivity.fromNetworkType(activeNetwork.getType());
        } else {
            return Connectivity.OFFLINE;
        }
    }
    //endregion

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Authentication result
        if (requestCode == AUTH_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                mAccessToken = response.getAccessToken();
                mSpotifyData = new SpotifyData(mAccessToken);

                Config playerConfig = new Config(this, mAccessToken, CLIENT_ID);
                Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                    @Override
                    public void onInitialized(SpotifyPlayer spotifyPlayer) {
                        mPlayer = spotifyPlayer;
                        mPlayer.addConnectionStateCallback(HostActivity.this);
                        mPlayer.addNotificationCallback(HostActivity.this);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        ErrorHandler.handleExeptionWithSnackbar(new Exception(throwable), "Could not initialize player");
                    }
                });
                getUserInfo();
            }
        }

        // AddSongActivity result
        if (requestCode == 333) {
            if (resultCode == AddSongActivity.RESULT_OK) {
                String songId = intent.getStringExtra("uri");
                mRoomService.addSong(songId);
            }
        }
    }

    //region Playback events
    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        ErrorHandler.handleMessege("Playback event received: " + playerEvent.name());
        mCurrentPlaybackState = mPlayer.getPlaybackState();
        mMetadata = mPlayer.getMetadata();
        if (queued) {
            if (mMetadata.nextTrack == null) {
                queued = false;
            }
        }
        if (playerEvent == PlayerEvent.kSpPlaybackNotifyPlay) {
            mRoomService.announcePlayTime(mCurrentPlaybackState.positionMs);
            btnPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_circle_white_48dp));
            btnPause.setBackground(getResources().getDrawable(R.drawable.ic_pause_circle_white_48dp));
            seekUpdation();
        }
        if (playerEvent == PlayerEvent.kSpPlaybackNotifyPause) {
            mRoomService.announcePause(mCurrentPlaybackState.positionMs);
            btnPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_circle_white_48dp));
            btnPause.setBackground(getResources().getDrawable(R.drawable.ic_play_circle_white_48dp));
        }
        if (playerEvent == PlayerEvent.kSpPlaybackNotifyTrackChanged) {
            if(mRoomService.skipQueued) mRoomService.skipQueued = false;
        }
        if (playerEvent == PlayerEvent.kSpPlaybackNotifyTrackDelivered) {

        }
        updatePlayerView();
    }


    @Override
    public void onPlaybackError(Error error) {
        ErrorHandler.handleMessege(error.name());
        switch (error) {
            // Handle error type as necessary
            default:
                break;
        }
    }
    //endregion

    //region Callback methods
    @Override
    public void onLoggedIn() {
        ErrorHandler.handleMessegeWithToast("User logged in");
        mCurrentPlaybackState = mPlayer.getPlaybackState();
        mMetadata = mPlayer.getMetadata();
    }

    @Override
    public void onLoggedOut() {
        ErrorHandler.handleMessegeWithToast("User logged out");
        finish();
    }

    @Override
    public void onLoginFailed(Error error) {
        if (Objects.equals(error.toString(), "kSpErrorNeedsPremium")) {
            ErrorHandler.handleMessegeWithToast("Premium account needed to be a host");
            finish();
        }

        ErrorHandler.handleMessegeWithToast("Login failed");
        ErrorHandler.handleMessege(error.toString());

    }

    @Override
    public void onTemporaryError() {
        ErrorHandler.handleMessegeWithToast("Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        ErrorHandler.handleMessegeWithToast("Received connection message: " + message);
    }
    //endregion

    //region Random methods
    private void updatePlaylistView(ArrayList<String> songs) {
        mSpotifyData.getTracks(songs, new WrappedSpotifyCallback<Tracks>() {
            @Override
            public void success(Tracks tracks, retrofit.client.Response response) {

                final ArrayList<SongItem> results = new ArrayList<>();

                for (Track track : tracks.tracks) {
                    String song = track.name;
                    String artist = Utilities.convertArtistListToString(track.artists);
                    String album = track.album.name;
                    String uri = track.uri;
                    results.add(new SongItem(song, artist, album, uri));
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPlaylistAdapter.clear();
                        mPlaylistAdapter.addAll(results);
                    }
                });
            }
        });
    }

    public void getUserInfo() {

        mSpotifyData.getUser(new WrappedSpotifyCallback<UserPrivate>() {
            @Override
            public void success(UserPrivate userPrivate, retrofit.client.Response response) {
                mUser = new User(userPrivate.id, userPrivate.display_name,
                        userPrivate.email, userPrivate.country);

                mRoomService.createRoom(mUser.getEmail());
            }
        });
    }

    private void updateTextView(final int id, final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(id)).setText(text);
            }
        });
    }

    private void updatePlayerView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    updateTextView(R.id.tv_artist, mMetadata.currentTrack.artistName);
                    updateTextView(R.id.tv_songName, mMetadata.currentTrack.name);
                    updateTextView(R.id.tv_trackLenght,
                            Utilities.formatSeconds(mMetadata.currentTrack.durationMs));
                    Picasso.with(HostActivity.this).
                            load(mMetadata.currentTrack.albumCoverWebUrl)
                            .into((ImageView) findViewById(R.id.iv_albumArt));
                } catch (NullPointerException e) {
                    ErrorHandler.handleExeption(e);
                }
            }
        });
    }

    Runnable run = new Runnable() {
        @Override
        public void run() {
            seekUpdation();
        }
    };

    public void seekUpdation() {
        int procentageDone = Utilities.getProgressPercentage(
                mPlayer.getPlaybackState().positionMs,
                mPlayer.getMetadata().currentTrack.durationMs);
        sbTrack.setProgress(procentageDone);
        updateTextView(R.id.tv_trackTime,
                Utilities.formatSeconds(mPlayer.getPlaybackState().positionMs));

        if (procentageDone >= 90) {
            String currentUri = mPlayer.getMetadata().currentTrack.uri;
            if (TracksRepository.isNOTLastAdded(currentUri)) {
                TracksRepository.addToLastPlayed(currentUri);
                TracksRepository.generateTrack(mSpotifyData);
            }
        }
        if (procentageDone >= 98 && !requestedNext && !mRoomService.skipQueued) {
            requestNext();
            requestedNext = true;
        } else if (procentageDone <= 5) {
            requestedNext = false;
        }
        if (updateCount >= 3) {
            if (mCurrentPlaybackState.isPlaying) {
                mRoomService.announcePlayTime(mPlayer.getPlaybackState().positionMs);
            } else {
                mRoomService.announcePause(mPlayer.getPlaybackState().positionMs);
            }
            updateCount = 0;
        } else {
            updateCount++;
        }
        mHandler.postDelayed(run, 1000);

    }

    private void requestNext() {
        String generatedTrackUri = TracksRepository.getFromGeneratedTracks();
        Log.d(TAG, "requestNext: " + generatedTrackUri);
        mRoomService.playNextSong(generatedTrackUri);
    }

    private void queueNext() {
        String songid = mRoomService.getCurrent();
        if (firstRun) {
            setUpFirstTrack(songid);
        } else if (mPlayer.getMetadata().nextTrack == null) {
            mPlayer.queue(mOperationCallback, songid);
        }

        //mPlayer.skipToNext(mOperationCallback);
    }

    private void setUpFirstTrack(String songid) {
        findViewById(R.id.root_player).setVisibility(View.VISIBLE);
        findViewById(R.id.ll_start).setVisibility(View.INVISIBLE);
        mPlayer.playUri(mOperationCallback, songid, 0, 0);
        mRoomService.refreshCurrentSong();
        firstRun = false;
        queued = true;
    }

    private void changeViews(Button button) {
        LinearLayout player = findViewById(R.id.root_player);
        LinearLayout playlist = findViewById(R.id.root_playlist);

        if (!firstRun) {
            if (player.getVisibility() == View.VISIBLE) {
                player.setVisibility(View.INVISIBLE);
                playlist.setVisibility(View.VISIBLE);
            } else {
                player.setVisibility(View.VISIBLE);
                playlist.setVisibility(View.INVISIBLE);
            }
        }


    }

    protected void changeBitRate() {
        final AlertDialog.Builder mBuilder = new AlertDialog.Builder(HostActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.dialog_bitratesettings, null);
        final String[] bitratesValues = getResources().getStringArray(R.array.bitratesValues);
        final PlaybackBitrate[] bitrates = {BITRATE_LOW, BITRATE_NORMAL, BITRATE_HIGH};
        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        Button btn_low = (Button) mView.findViewById(R.id.btn_lowBitrate);
        btn_low.setText(bitratesValues[0]);
        btn_low.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPlayer.setPlaybackBitrate(mOperationCallback, bitrates[0]);
                ErrorHandler.handleMessegeWithToast("Bit rate = " + bitratesValues[0]);
                dialog.dismiss();
            }
        });
        Button btn_normal = (Button) mView.findViewById(R.id.btn_normalBitrate);
        btn_normal.setText(bitratesValues[1]);
        btn_normal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPlayer.setPlaybackBitrate(mOperationCallback, bitrates[1]);
                ErrorHandler.handleMessegeWithToast("Bit rate = " + bitratesValues[1]);
                dialog.dismiss();
            }
        });
        Button btn_high = (Button) mView.findViewById(R.id.btn_highBitrate);
        btn_high.setText(bitratesValues[2]);
        btn_high.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPlayer.setPlaybackBitrate(mOperationCallback, bitrates[2]);
                ErrorHandler.handleMessegeWithToast("Bit rate = " + bitratesValues[2]);
                dialog.dismiss();
            }
        });
    }

    public void showInfoRoom() {
        final AlertDialog.Builder mBuilder = new AlertDialog.Builder(HostActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.dialog_info, null);
        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        TextView tvUsername = mView.findViewById(R.id.tv_userName);
        TextView tvCountry = mView.findViewById(R.id.tv_country);
        TextView tvEmail = mView.findViewById(R.id.tv_email);
        TextView tvID = mView.findViewById(R.id.tv_userID);
        tvUsername.setText(mUser.getDisplayName());
        tvCountry.setText(mUser.getCountry());
        tvEmail.setText(mUser.getEmail());
        tvID.setText(mUser.getId());
        dialog.show();
    }

    private void skipToEnd() {
        String currentUri = mPlayer.getMetadata().currentTrack.uri;
        if (TracksRepository.isNOTLastAdded(currentUri)) {
            TracksRepository.addToLastPlayed(currentUri);
            TracksRepository.generateTrack(mSpotifyData);
        }

        long dur = mPlayer.getMetadata().currentTrack.durationMs;
        long skip = dur - 1000;
        mPlayer.seekToPosition(mOperationCallback, (int)skip);
    }

    private void setUpRoomChangeHandler() {
        mRoomService.subscribe(new RoomService.OnChangeSubscriber() {
            @Override
            public void callback(final String[] changes) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < changes.length; i++) {
                            Log.d("Change", changes[i]);
                            switch (changes[i]) {
                                case RoomService.PAST_SONGS_UPDATED:
                                    ArrayList<String> pastSongs = mRoomService.getPastSongs();
                                    break;
                                case RoomService.PLAYTIME_UPDATED:
                                    long playTime = mRoomService.getPlayTime();
                                    break;
                                case RoomService.ROOM_UPDATED:
                                    txLoginCode.setText(Long.toString(mRoomService.getRoom().getLoginCode()));
                                    setVisibilityForUiElements(View.VISIBLE);
                                    findViewById(R.id.root_player).setVisibility(View.INVISIBLE);
                                    findViewById(R.id.ll_start).setVisibility(View.VISIBLE);
                                    break;
                                case RoomService.SONG_LIST_UPDATED:
                                    updatePlaylistView(mRoomService.getSongs());
                                    if (mRoomService.isSkipped()) {
                                        skipToEnd();
                                    }
                                    if (firstRun) {
                                        mRoomService.refreshCurrentSong();
                                    }
                                    break;
                                case RoomService.SONG_UPDATED:
                                    queueNext();
                                    break;
                                case RoomService.STATUS_UPDATED:
                                    Log.w(TAG, "Playing status changed notification not handled. Remove it or change it");
                                    break;
                            }
                        }
                    }
                });
            }
        });
    }

    //endregion

    //region Destruction
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mNetworkStateReceiver);

        // Note that calling Spotify.destroyPlayer() will also remove any callbacks on whatever
        // instance was passed as the refcounted owner. So in the case of this particular example,
        // it's not strictly necessary to call these methods, however it is generally good practice
        // and also will prevent your application from doing extra work in the background when
        // paused.
        if (mPlayer != null) {
            mPlayer.removeNotificationCallback(HostActivity.this);
            mPlayer.removeConnectionStateCallback(HostActivity.this);
        }
    }

    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        mRoomService.disconnect(mUser.getEmail());
        super.onDestroy();
    }
    //endregion
}


