package com.latenightpenguin.groupdj;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.latenightpenguin.groupdj.NetworkServices.ServerHelper;
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

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.spotify.sdk.android.player.PlaybackBitrate.BITRATE_HIGH;
import static com.spotify.sdk.android.player.PlaybackBitrate.BITRATE_LOW;
import static com.spotify.sdk.android.player.PlaybackBitrate.BITRATE_NORMAL;

public class HostActivity extends AppCompatActivity implements
        SpotifyPlayer.NotificationCallback, ConnectionStateCallback {

    // CONSTANTS

    private static final String TAG = HostActivity.class.getSimpleName();

    private static final String CLIENT_ID = "1b02f619aa8142db8cd6d3d9bc3d505e";

    private static final String REDIRECT_URI = "lnpapp://callback";

    private static final int AUTH_CODE = 1337;

    // FIELDS

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

    private Call mCall;

    private OkHttpClient mOkHttpClient = new OkHttpClient();

    private Handler mHandler = new Handler();
    // UI ELEMENTS

    private Button btnAdd;
    private ImageButton btnPause;
    private ImageButton btnNext;
    private SeekBar sbTrack;
    private Button btnSettings;
    private Button btnInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        // AUTHENTIFICATION
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "user-read-email", "streaming"});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, AUTH_CODE, request);

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
                if (mCurrentPlaybackState != null
                        && mMetadata != null && mMetadata.nextTrack != null) {
                    mPlayer.skipToNext(mOperationCallback);
                }
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

    }

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
                    Log.i(TAG, "Network state changed: " + connectivity.toString());
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Authentication result
        if (requestCode == AUTH_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                mAccessToken = response.getAccessToken();
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
                        Toast.makeText(HostActivity.super.getApplicationContext(), "Could not initialize player", Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Could not initialize player: " + throwable.getMessage());
                    }
                });
                getUserInfo();
            }
        }

        // AddSongActivity result
        if (requestCode == 333){
            if(resultCode == AddSongActivity.RESULT_OK){
                mPlayer.queue(mOperationCallback, intent.getStringExtra("uri"));
            }
        }
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d(TAG, "Playback event received: " + playerEvent.name());
        mCurrentPlaybackState = mPlayer.getPlaybackState();
        mMetadata = mPlayer.getMetadata();
        Log.d(TAG, "Playback State: " + mCurrentPlaybackState.toString());
        Log.d(TAG, "Metadata: " + mMetadata.toString());
        if (playerEvent == PlayerEvent.kSpPlaybackNotifyPlay) {
            btnPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_media_pause));
            seekUpdation();
        }
        if (playerEvent == PlayerEvent.kSpPlaybackNotifyPause) {
            btnPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_media_play));
        }
        if (playerEvent == PlayerEvent.kSpPlaybackNotifyTrackChanged) {

        }
        updateView();
    }


    @Override
    public void onPlaybackError(Error error) {
        Log.d(TAG, "Playback error received: " + error.name());
        switch (error) {
            // Handle error type as necessary
            default:
                break;
        }
    }

    //
    // CALLBACK METHODS
    //

    @Override
    public void onLoggedIn() {
        Log.d(TAG, "User logged in");
        Toast.makeText(this, "User logged in", Toast.LENGTH_LONG).show();

        // ROOM CREATION
        String userID = "test@test.com";

        TextView status = findViewById(R.id.tv_RoomId);

        ServerHelper serverHelper = new ServerHelper();
        serverHelper.registerUser(userID, status);

        mCurrentPlaybackState = mPlayer.getPlaybackState();
        mMetadata = mPlayer.getMetadata();

        mPlayer.playUri(mOperationCallback, "spotify:track:10ViidwjGLCfVtGPfdcszR", 0, 0);
    }

    @Override
    public void onLoggedOut() {
        Toast.makeText(this, "User logged out", Toast.LENGTH_LONG).show();
        Log.d(TAG, "User logged out");
        finish();
    }

    @Override
    public void onLoginFailed(Error error) {
        if (Objects.equals(error.toString(), "kSpErrorNeedsPremium")) {
            Toast.makeText(this, "Premium account needed to be a host", Toast.LENGTH_LONG).show();
            //finish();
        }
        Log.d(TAG, "Login failed : " + error.toString());
    }

    @Override
    public void onTemporaryError() {
        Log.d(TAG, "Temporary error occurred");
        Toast.makeText(this, "Temporary error occurred", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d(TAG, "Received connection message: " + message);
        Toast.makeText(this, "Received connection message: " + message, Toast.LENGTH_LONG).show();
    }

    // Random methods

    public void getUserInfo() {

        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me")
                .addHeader("Authorization", "Bearer " + mAccessToken)
                .build();

        cancelCall();
        mCall = mOkHttpClient.newCall(request);

        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(HostActivity.this, "Failed to fetch data: " + e, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    final JSONObject jsonObject = new JSONObject(response.body().string());
                    mUser = new User(jsonObject.getString("id"),
                            jsonObject.getString("display_name"),
                            jsonObject.getString("email"),
                            jsonObject.getString("country"));
                } catch (JSONException e) {
                    Toast.makeText(HostActivity.this, "Failed to parse data: " + e, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void cancelCall() {
        if (mCall != null) {
            mCall.cancel();
        }
    }

    private void updateTextView(final int id, final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(id)).setText(text);
            }
        });
    }

    private void updateView() {
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
                    Log.e(TAG, "Metadata is null: " + mMetadata);
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
        sbTrack.setProgress(Utilities.getProgressPercentage(
                mPlayer.getPlaybackState().positionMs,
                mPlayer.getMetadata().currentTrack.durationMs));
        updateTextView(R.id.tv_trackTime,
                Utilities.formatSeconds(mPlayer.getPlaybackState().positionMs));
        mHandler.postDelayed(run, 1000);
    }

    // DESTRUCTION

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
        super.onDestroy();
    }

    protected void changeBitRate(){
        final AlertDialog.Builder mBuilder = new AlertDialog.Builder(HostActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.dialog_bitratesettings, null);
        final String[] bitratesValues = getResources().getStringArray(R.array.bitratesValues);
        final PlaybackBitrate[] bitrates = {BITRATE_LOW,BITRATE_NORMAL,BITRATE_HIGH};
        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.show();
        Button btn_low = (Button) mView.findViewById(R.id.btn_lowBitrate);
        btn_low.setText(bitratesValues[0]);
        btn_low.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                mPlayer.setPlaybackBitrate(mOperationCallback, bitrates[0]);
                Toast.makeText(HostActivity.this, "Bit rate = "+ bitratesValues[0], Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        Button btn_normal = (Button) mView.findViewById(R.id.btn_normalBitrate);
        btn_normal.setText(bitratesValues[1]);
        btn_normal.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                mPlayer.setPlaybackBitrate(mOperationCallback, bitrates[1]);
                Toast.makeText(HostActivity.this, "Bit rate = "+ bitratesValues[1], Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        Button btn_high = (Button) mView.findViewById(R.id.btn_highBitrate);
        btn_high.setText(bitratesValues[2]);
        btn_high.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                mPlayer.setPlaybackBitrate(mOperationCallback, bitrates[2]);
                Toast.makeText(HostActivity.this, "Bit rate = "+ bitratesValues[2], Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

    public void showInfoRoom(){
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
}
