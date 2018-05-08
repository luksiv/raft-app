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
import android.widget.Toast;

import com.latenightpenguin.groupdj.NetworkServices.ServerAPI.ICallback;
import com.latenightpenguin.groupdj.NetworkServices.ServerAPI.IServerHelper;
import com.latenightpenguin.groupdj.NetworkServices.ServerAPI.RoomInfo;
import com.latenightpenguin.groupdj.NetworkServices.ServerAPI.ServerFactory;
import com.latenightpenguin.groupdj.NetworkServices.ServerAPI.SongConverter;
import com.latenightpenguin.groupdj.NetworkServices.ServerAPI.WebSocketStatus;
import com.latenightpenguin.groupdj.NetworkServices.SpotifyAPI.SpotifyData;
import com.latenightpenguin.groupdj.NetworkServices.SpotifyAPI.WrappedSpotifyCallback;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import kaaes.spotify.webapi.android.models.Recommendations;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import kaaes.spotify.webapi.android.models.UserPrivate;
import okhttp3.Call;
import okhttp3.OkHttpClient;

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
    private RoomInfo mRoom;
    private Call mCall;
    private OkHttpClient mOkHttpClient = new OkHttpClient();
    private Handler mHandler = new Handler();
    private ArrayList<String> mSongs;
    private PlaylistArrayAdapter mPlaylistAdapter;
    private SpotifyData mSpotifyData;
    private IServerHelper mServerHelper;

    private Boolean requestUsed = false;
    private Boolean firstRun = true;
    private Boolean voted = false;
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
    //endregiongit

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        // ERROR HANDLER
        Thread.setDefaultUncaughtExceptionHandler(new ErrorHandler());
        ErrorHandler.setContext(HostActivity.this);
        ErrorHandler.setView(findViewById(R.id.root_hostactivity));

        authentication();
        setUpElements();

        mServerHelper = ServerFactory.make(getResources().getString(R.string.url));
        setUpWebSocketCallbacks();
        mRoom = new RoomInfo();

        TracksRepository.setUp();

    }

    //region Methods that onCreate uses
    private void setUpElements() {
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
                if(!voted) {
                    ICallback callback = new ICallback() {
                        @Override
                        public void execute(String response) {
                            Toast.makeText(HostActivity.this, "You voted", Toast.LENGTH_SHORT).show();
                        }
                    };
                    mServerHelper.voteSkipSong(mRoom, callback);
                    voted = true;
                }
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
                updatePlaylist();
                ServerFactory.AdditionalCallbacks callbacks = ServerFactory.getAdditionalCallbacks(mServerHelper);
                if(callbacks != null) {
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
    }

    private void authentication() {
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "user-read-email", "streaming"});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, AUTH_CODE, request);
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
                   // Log.i(TAG, "Network state changed: " + connectivity.toString());
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

        if (mServerHelper != null && mServerHelper.getWebSocketStatus() == WebSocketStatus.DISCONNECTED) {
            mServerHelper.connectWebSocket();
            mServerHelper.setRoomUpdates(mRoom.getId());
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
                    //    Toast.makeText(HostActivity.super.getApplicationContext(), "Could not initialize player", Toast.LENGTH_LONG).show();
                    //    Log.e(TAG, "Could not initialize player: " + throwable.getMessage());
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
                ICallback addSongCallback = new ICallback() {
                    @Override
                    public void execute(String response) {
                     //   Toast.makeText(HostActivity.this, "Song added", Toast.LENGTH_SHORT).show();
                        ErrorHandler.handleMessegeWithToast("Song added");
                        if (firstRun) {
                            queueNext();
                        }
                        updatePlaylist();
                    }
                };
                mServerHelper.addSong(mRoom, songId, addSongCallback);

            }
        }
    }

    //region Playback events
    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        //Log.d(TAG, "Playback event received: " + playerEvent.name());
        ErrorHandler.handleMessege("Playback event received: " + playerEvent.name());
        mCurrentPlaybackState = mPlayer.getPlaybackState();
        mMetadata = mPlayer.getMetadata();
        //Log.d(TAG, "Playback State: " + mCurrentPlaybackState.toString());
        //Log.d(TAG, "Metadata: " + mMetadata.toString());
        if (playerEvent == PlayerEvent.kSpPlaybackNotifyPlay) {
            mServerHelper.announcePlayTime(mCurrentPlaybackState.positionMs);
            btnPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_circle_white_48dp));
            btnPause.setBackground(getResources().getDrawable(R.drawable.ic_pause_circle_white_48dp));
            seekUpdation();
        }
        if (playerEvent == PlayerEvent.kSpPlaybackNotifyPause) {
            mServerHelper.announcePause(mCurrentPlaybackState.positionMs);
            btnPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_circle_white_48dp));
            btnPause.setBackground(getResources().getDrawable(R.drawable.ic_play_circle_white_48dp));
        }
        if (playerEvent == PlayerEvent.kSpPlaybackNotifyTrackChanged) {

        }
        if (playerEvent == PlayerEvent.kSpPlaybackNotifyTrackDelivered) {

        }
        updatePlayerView();
    }


    @Override
    public void onPlaybackError(Error error) {
        //Log.d(TAG, "Playback error received: " + error.name());
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
        //Log.d(TAG, "User logged in");
        //Toast.makeText(this, "User logged in", Toast.LENGTH_LONG).show();
        ErrorHandler.handleMessegeWithToast("User logged in");
        mCurrentPlaybackState = mPlayer.getPlaybackState();
        mMetadata = mPlayer.getMetadata();
    }

    @Override
    public void onLoggedOut() {
       // Toast.makeText(this, "User logged out", Toast.LENGTH_LONG).show();
        //Log.d(TAG, "User logged out");
        ErrorHandler.handleMessegeWithToast("User logged out");
        finish();
    }

    @Override
    public void onLoginFailed(Error error) {
        if (Objects.equals(error.toString(), "kSpErrorNeedsPremium")) {
            //Toast.makeText(this, "Premium account needed to be a host", Toast.LENGTH_LONG).show();
            ErrorHandler.handleMessegeWithToast("Premium account needed to be a host");
            //finish();
        }
        //Log.d(TAG, "Login failed : " + error.toString());

        ErrorHandler.handleMessegeWithToast("Login failed");
        ErrorHandler.handleMessege(error.toString());

    }

    @Override
    public void onTemporaryError() {
        //Log.d(TAG, "Temporary error occurred");
        //Toast.makeText(this, "Temporary error occurred", Toast.LENGTH_LONG).show();
        ErrorHandler.handleMessegeWithToast("Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        //Log.d(TAG, "Received connection message: " + message);
        //Toast.makeText(this, "Received connection message: " + message, Toast.LENGTH_LONG).show();
        ErrorHandler.handleMessegeWithToast("Received connection message: " + message);
    }
    //endregion

    //region Random methods

    private void putSongToPlaylist(String songId) {
        ICallback addSongCallback = new ICallback() {
            @Override
            public void execute(String response) {
                Toast.makeText(HostActivity.this, "Song added", Toast.LENGTH_SHORT).show();
                updatePlaylist();
            }
        };
        mServerHelper.addSong(mRoom, songId, addSongCallback);
    }

    private void updatePlaylist() {
        ICallback getSongsCallback = new ICallback() {
            @Override
            public void execute(String response) {
                mSongs = SongConverter.convertToList(response);
                for (String song : mSongs) {
                    Log.d(TAG, song);
                    ErrorHandler.handleMessege(song);
                }
                updatePlaylistView();
            }
        };
        mServerHelper.getSongs(mRoom, getSongsCallback);
    }

    private void updatePlaylistView() {
        mSpotifyData.getTracks(mSongs, new WrappedSpotifyCallback<Tracks>() {
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
                createRoom();
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
                    //Log.e(TAG, "Metadata is null: " + mMetadata);
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


        if (procentageDone >= 90 && !requestUsed) {
            String currentUri = mPlayer.getMetadata().currentTrack.uri;
            if (TracksRepository.isNOTLastAdded(currentUri)) {
                TracksRepository.addToLastPlayed(currentUri);
                generatePlaylist();

            }
        }
        if (procentageDone >= 99 && !requestUsed) {
            queueNext();
        }
        mHandler.postDelayed(run, 1000);

    }

    private void queueNext() {
        if (mPlayer.getMetadata().nextTrack == null) {
            final ICallback callback = new ICallback() {
                @Override
                public void execute(String response) {
                    voted = false;
                    try {
                       // Toast.makeText(HostActivity.this, "Next song queued", Toast.LENGTH_SHORT).show();
                        ErrorHandler.handleMessegeWithToast("Next song queued");
                       // Log.d(TAG, response.toString());
                        ErrorHandler.handleMessege(response.toString());
                        String uri;
                        if (response.toString().isEmpty() && !TracksRepository.generatedTracks.isEmpty()) {
                            String generatedTrackUri = TracksRepository.getFromGeneratedTracks();
                            putSongToPlaylist(generatedTrackUri.split(":")[2]);
                        }

                        JSONObject nextTrack = new JSONObject(response);
                        uri = nextTrack.getString("song");

                        if (firstRun) {
                            mPlayer.playUri(mOperationCallback, uri, 0, 0);
                            firstRun = false;
                        } else {
                            mPlayer.queue(mOperationCallback, uri);
                        }
                        requestUsed = false;
                    } catch (JSONException e) {
                       // e.printStackTrace();
                        ErrorHandler.handleExeption(e);
                    }
                }
            };
            requestUsed = true;
            mServerHelper.playNextSong(mRoom, callback);
        }

    }

    private void generatePlaylist() {
        mSpotifyData.getRecomendationList(mSpotifyData.convertArrayToString(TracksRepository.toArray()), new WrappedSpotifyCallback<Recommendations>() {
            @Override
            public void success(Recommendations recommendations, retrofit.client.Response response) {
                super.success(recommendations, response);
               // Toast.makeText(HostActivity.this, "Generated track", Toast.LENGTH_SHORT).show();
                TracksRepository.addToGeneratedTracks(SpotifyData.ConvertRecomendedTracks(recommendations).get(0).getUri());
            }
        });
    }


    private void changeViews(Button button) {
        LinearLayout player = findViewById(R.id.root_player);
        LinearLayout playlist = findViewById(R.id.root_playlist);

        if (player.getVisibility() == View.VISIBLE) {
            player.setVisibility(View.INVISIBLE);
            playlist.setVisibility(View.VISIBLE);
            //button.setText("Show player");
        } else {
            player.setVisibility(View.VISIBLE);
            playlist.setVisibility(View.INVISIBLE);
            //button.setText("Show playlist");
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
               // Toast.makeText(HostActivity.this, "Bit rate = " + bitratesValues[0], Toast.LENGTH_SHORT).show();
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
                //Toast.makeText(HostActivity.this, "Bit rate = " + bitratesValues[1], Toast.LENGTH_SHORT).show();
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
                //Toast.makeText(HostActivity.this, "Bit rate = " + bitratesValues[2], Toast.LENGTH_SHORT).show();
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

    public void createRoom() {
        final TextView status = (TextView) findViewById(R.id.tv_RoomId);

        final ICallback callback = new ICallback() {
            @Override
            public void execute(String response) {
                ICallback insideCallback = new ICallback() {
                    @Override
                    public void execute(String response) {
                        if (response != null) {
                            try {
                                JSONObject roomInfo = new JSONObject(response.toString());
                                int roomId = roomInfo.getInt("id");
                                int loginCode = roomInfo.getInt("logincode");

                                mRoom.setId(roomId);
                                mRoom.setLoginCode(loginCode);
                                mServerHelper.setRoomUpdates(roomId);

                                status.setText("Login code is " + String.valueOf(loginCode));
                            } catch (JSONException e) {
                                Log.d("MusicDJ", response.toString());
                                status.setText("Error parsing response");
                                ErrorHandler.handleExeption(e);
                            }
                        }
                    }
                };
                mServerHelper.createRoom(mUser.getEmail(), insideCallback);
            }
        };
        mServerHelper.registerUser(mUser.getEmail(), callback);
    }

    private void setUpWebSocketCallbacks() {
        ICallback playingNext = new ICallback() {
            @Override
            public void execute(String response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                      //  Toast.makeText(HostActivity.this, "Next Song is played", Toast.LENGTH_SHORT).show();
                        ErrorHandler.handleMessegeWithToast("Next Song is played");
                        updatePlaylist();
                    }
                });
            }
        };

        ICallback songAdded = new ICallback() {
            @Override
            public void execute(String response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (firstRun) {
                            queueNext();
                        }
                        //Toast.makeText(HostActivity.this, "Someone added a song", Toast.LENGTH_SHORT).show();
                        ErrorHandler.handleMessegeWithToast("Someone added a song");
                        updatePlaylist();
                    }
                });
            }
        };

        ICallback paused = new ICallback() {
            @Override
            public void execute(String response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(HostActivity.this, "Song paused", Toast.LENGTH_SHORT).show();
                        ErrorHandler.handleMessegeWithToast("Song paused");
                    }
                });
            }
        };

        ICallback playTime = new ICallback() {
            @Override
            public void execute(final String response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(HostActivity.this, "play time: " + response, Toast.LENGTH_SHORT).show();
                        ErrorHandler.handleMessegeWithToast("play time: " + response);
                    }
                });
            }
        };

        ICallback connectedRoom = new ICallback() {
            @Override
            public void execute(final String response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(HostActivity.this, "play time: " + response, Toast.LENGTH_SHORT).show();
                        ErrorHandler.handleMessegeWithToast(response);
                    }
                });
            }
        };

        ICallback skipCallback = new ICallback() {
            @Override
            public void execute(final String response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(HostActivity.this, "play time: " + response, Toast.LENGTH_SHORT).show();
                        ErrorHandler.handleMessegeWithToast("song skipped");
                        queueNext();
                    }
                });
            }
        };

        mServerHelper.setPlayingNextCallback(playingNext);
        mServerHelper.setSongAddedCallback(songAdded);
        mServerHelper.setSongPausedCallback(paused);
        mServerHelper.setSongPlayTimeCallback(playTime);
        mServerHelper.setConnectedToRoomCallback(connectedRoom);
        mServerHelper.setSongSkippedCallback(skipCallback);
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
        mServerHelper.closeWebSocket();
        super.onDestroy();
    }
    //endregion
}


