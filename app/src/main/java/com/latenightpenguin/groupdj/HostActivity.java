package com.latenightpenguin.groupdj;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
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
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import java.util.Random;

public class HostActivity extends AppCompatActivity implements
        SpotifyPlayer.NotificationCallback, ConnectionStateCallback{
    // Static variables
    private static final String TAG = HostActivity.class.getSimpleName();
    private static final String CLIENT_ID = "1b02f619aa8142db8cd6d3d9bc3d505e";
    private static final String REDIRECT_URI = "lnpapp://callback";
    private static final int REQUEST_CODE = 1337;

    // Obejects
    private Player mPlayer;

    // Declaring view elements
    ImageButton btnAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);
        // AUTHENTIFICATION
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);

        // ROOM CREATION
        Random rand = new Random();
        int userID = rand.nextInt();

        TextView status = (TextView)findViewById(R.id.tw_RoomId);

        ServerHelper serverHelper = new ServerHelper();
        serverHelper.createRoom(userID, status);

        btnAdd = findViewById(R.id.btn_AddSong);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HostActivity.super.getApplicationContext(), AddSongActivity.class);
                startActivity(intent);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
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
        Log.d(TAG, "Playback event received: " + playerEvent.name());
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
}
