package com.latenightpenguin.groupdj;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.latenightpenguin.groupdj.NetworkServices.ServerHelper;
import com.latenightpenguin.groupdj.NetworkServices.ServerRequest;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Metadata;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HostActivity extends AppCompatActivity implements
        SpotifyPlayer.NotificationCallback, ConnectionStateCallback{
    // Static variables
    private static final String TAG = HostActivity.class.getSimpleName();
    private static final String CLIENT_ID = "1b02f619aa8142db8cd6d3d9bc3d505e";
    private static final String REDIRECT_URI = "lnpapp://callback";
    private static final int REQUEST_CODE = 1337;

    private final Player.OperationCallback mOperationCallback = new Player.OperationCallback() {
        @Override
        public void onSuccess() {
            Log.e("Callback", "OK!");
        }

        @Override
        public void onError(Error error) {
            Log.e("Callback","ERROR:" + error);
        }
    };

    // variables
    private String mAccessToken;
    private Metadata mMetadata;
    // Obejects
    private Player mPlayer;
    User mUser;
    private Call mCall;
    private OkHttpClient mOkHttpClient = new OkHttpClient();

    // Declaring view elements
    ImageButton btnAdd;
    Button btnPlay;
    Button btnPause;
    Button btnNext;
    Button btnPrev;
    TextView twSongName;
    TextView twArtist;
    ImageView ivAlbumArt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        // AUTHENTIFICATION
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "user-read-email", "streaming"});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);

        btnAdd = findViewById(R.id.btn_AddSong);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HostActivity.super.getApplicationContext(), AddSongActivity.class);
                startActivity(intent);
            }
        });
        btnPlay = findViewById(R.id.btn_play);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mPlayer != null){
                    mPlayer.resume(mOperationCallback);
                }
            }
        });
        btnPause = findViewById(R.id.btn_pause);
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mPlayer != null){
                    mPlayer.pause(mOperationCallback);
                }
            }
        });
        btnNext = findViewById(R.id.btn_next);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mPlayer != null){
                    mPlayer.skipToNext(mOperationCallback);
                }
            }
        });
        btnPrev = findViewById(R.id.btn_previous);
        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mPlayer != null){
                    mPlayer.skipToPrevious(mOperationCallback);
                }
            }
        });
        twArtist = findViewById(R.id.tw_artist);
        twSongName = findViewById(R.id.tw_songName);
        ivAlbumArt = findViewById(R.id.iv_albumArt);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == REQUEST_CODE) {
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
    }

    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        // TODO: Clean up the macaroni code with proper playerEvent handling
        Log.d(TAG, "Playback event received: " + playerEvent.name());
        mMetadata = mPlayer.getMetadata();
        try {
            Log.d(TAG, "Metadata: " + mMetadata.toString());
            updateTextView(R.id.tw_album, "Song: " + mMetadata.currentTrack.albumName);
            updateTextView(R.id.tw_artist, "Artist: " + mMetadata.currentTrack.artistName);
            updateTextView(R.id.tw_songName, "Album: " + mMetadata.currentTrack.name);
            Picasso.with(HostActivity.this).
                    load(mMetadata.currentTrack.albumCoverWebUrl).into(ivAlbumArt);
            if(mMetadata.nextTrack != null) {
                updateTextView(R.id.tw_nextTrack, "Next song: "
                        + mMetadata.nextTrack.name + " by " + mMetadata.nextTrack.artistName);
            } else {
                updateTextView(R.id.tw_nextTrack, "Next song: none");
            }
            if(mMetadata.prevTrack != null) {
                updateTextView(R.id.tw_prevTrack, "Previous song: "
                        + mMetadata.prevTrack.name + " by " + mMetadata.prevTrack.artistName);
            } else {
                updateTextView(R.id.tw_prevTrack, "Previous song: none");
            }
        } catch (NullPointerException e){
            Log.e(TAG, "Metadata is null: " + mMetadata);
        }
        switch (playerEvent) {
            // Handle event type as necessary
            default:
                break;
        }
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

    @Override
    public void onLoggedIn() {
        Log.d(TAG, "User logged in");
        Toast.makeText(this, "User logged in", Toast.LENGTH_LONG).show();

        // ROOM CREATION
        // TODO: User id is not necessary, remove it and change it with spotify email or id.
        Random rand = new Random();
        int userID = rand.nextInt();

        TextView status = (TextView)findViewById(R.id.tw_RoomId);

        ServerHelper serverHelper = new ServerHelper();
        serverHelper.createRoom(userID, status);

        // This is the line that plays a song.
        mPlayer.playUri(null, "spotify:track:3K4HG9evC7dg3N0R9cYqk4", 0, 0);
    }

    @Override
    public void onLoggedOut() {
        Toast.makeText(this, "User logged out", Toast.LENGTH_LONG).show();
        Log.d(TAG, "User logged out");
    }

    @Override
    public void onLoginFailed(Error var1) {
        if(var1.toString() == "kSpErrorNeedsPremium"){
            Toast.makeText(this, "Premium account needed to be a host", Toast.LENGTH_LONG).show();
            finish();
        }
        Log.d(TAG, "Login failed : "+ var1.toString());
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
                    updateUserView();
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

    private void updateUserView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView)findViewById(R.id.tw_user)).setText(mUser.toString());
            }
        });
    }

    private void updateTextView(final int id, final String text){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView)findViewById(id)).setText(text);
            }
        });
    }


    // DEMONSTRATIONAL PURPOSES ONLY
    // TODO: Remove queue1,2,3 after we show them off
    public void queue1(View view){
        mPlayer.queue(mOperationCallback, "spotify:track:3VzJE6yGuj8fDExUh6TLnc");
    }
    public void queue2(View view){
        mPlayer.queue(mOperationCallback, "spotify:track:6Gn02ZC8juXwQ10Xk7ACXx");
    }
    public void queue3(View view){
        mPlayer.queue(mOperationCallback, "spotify:track:0VgkVdmE4gld66l8iyGjgx");
    }

}
