package com.latenightpenguin.groupdj;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.latenightpenguin.groupdj.NetworkServices.ServerHelper;
import com.latenightpenguin.groupdj.NetworkServices.ServerRequest;
import com.latenightpenguin.groupdj.NetworkServices.SpotifyAPI.SpotifyData;
import com.latenightpenguin.groupdj.NetworkServices.SpotifyAPI.WrappedSpotifyCallback;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import kaaes.spotify.webapi.android.models.UserPrivate;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class ClientActivity extends AppCompatActivity {

    private static final String TAG = ClientActivity.class.getSimpleName();
    private static final String CLIENT_ID = "1b02f619aa8142db8cd6d3d9bc3d505e";
    private static final String REDIRECT_URI = "lnpapp://callback";
    private static final int REQUEST_CODE = 1337;

    private String mAccessToken;

    User mUser;
    RoomInfo mRoom;
    ArrayList<String> mSongs;

    private OkHttpClient mOkHttpClient = new OkHttpClient();
    private Call mCall;

    // Declaring view elements
    ImageButton btnAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        mRoom = new RoomInfo();

        try {
            mRoom.setLoginCode(getIntent().getIntExtra("roomId", 0));
        } catch (Exception e) {
            Log.v(TAG, e.getMessage());
        }

        btnAdd = findViewById(R.id.btn_AddSong);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ClientActivity.super.getApplicationContext(), AddSongActivity.class);
                intent.putExtra("accessToken", mAccessToken);
                //startActivity(intent);
                startActivityForResult(intent, 333);
            }
        });

        Button addSongButton = findViewById(R.id.btn_add_song);
        addSongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServerHelper serverHelper = new ServerHelper();
                ServerRequest.Callback addSongCallback = new ServerRequest.Callback() {
                    @Override
                    public void execute(String response) {
                        Toast.makeText(ClientActivity.this, "Song added", Toast.LENGTH_SHORT).show();
                    }
                };
                serverHelper.addSong(mRoom, "testSong", addSongCallback);
            }
        });
        Button getSongsButton = findViewById(R.id.btn_get_songs);
        getSongsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ServerHelper serverHelper = new ServerHelper();
                ServerRequest.Callback getSongsCallback = new ServerRequest.Callback() {
                    @Override
                    public void execute(String response) {
                        mSongs = serverHelper.convertToList(response);
                        for(String song : mSongs) {
                            Log.d("MusicDJ", song);
                        }
                    }
                };
                serverHelper.getSongs(mRoom, getSongsCallback);
            }
        });
        Button playNextButton = findViewById(R.id.btn_play_next);
        playNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ServerHelper serverHelper = new ServerHelper();
                ServerRequest.Callback playNextCallback = new ServerRequest.Callback() {
                    @Override
                    public void execute(String response) {
                        String song = "";
                        if(response == null || response.equals("")){
                            Toast.makeText(ClientActivity.this, "Nebėra dainų eilėje", Toast.LENGTH_SHORT);
                        }
                        song = serverHelper.getSongId(response);
                        Log.d("MusicDJ", song);
                    }
                };
                serverHelper.playNextSong(mRoom, playNextCallback);
            }
        });

        // AUTHENTIFICATION
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-email"});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);

        //WRAPPER TESTING
        final Button test = findViewById(R.id.btn_test);
        final TextView testOutput = findViewById(R.id.test_output);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SpotifyData data = new SpotifyData(mAccessToken);

                data.getUser(new WrappedSpotifyCallback<UserPrivate>() {
                    @Override
                    public void success(UserPrivate userPrivate, retrofit.client.Response response) {
                        testOutput.setText(userPrivate.email + "\n" + userPrivate.id);
                    }
                });
                /*
                data.getTrack("09BxJMIz6CkunwqTPgpEWV",new WrappedSpotifyCallback<Track>() {
                    @Override
                    public void success(Track track, retrofit.client.Response response) {
                        testOutput.setText(track.name + "\n" + track.artists.get(0).name + "\n" + track.id);
                    }
                });
                data.searchTracks("Twilight Zone",new WrappedSpotifyCallback<TracksPager>() {
                    @Override
                    public void success(TracksPager tracks, retrofit.client.Response response) {
                        testOutput.setText(SpotifyData.ConvertTracks(tracks).get(0).getName() + "\n" + SpotifyData.ConvertTracks(tracks).get(0).getId());
                    }
                });
                */

        }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                mAccessToken = response.getAccessToken();
                getUserInfo();
            }
            if (response.getType() == AuthenticationResponse.Type.ERROR){
                Log.e("Authentification", response.getError());
            }
        }

        // AddSongActivity result
        if (requestCode == 333){
            if(resultCode == AddSongActivity.RESULT_OK){
                String songId = intent.getStringExtra("uri");

                ServerHelper serverHelper = new ServerHelper();
                ServerRequest.Callback addSongCallback = new ServerRequest.Callback() {
                    @Override
                    public void execute(String response) {
                        Toast.makeText(ClientActivity.this, "Song added", Toast.LENGTH_SHORT).show();
                    }
                };
                serverHelper.addSong(mRoom, songId, addSongCallback);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
                Toast.makeText(ClientActivity.this, "Failed to fetch data: " + e, Toast.LENGTH_SHORT).show();
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

                    connectToRoom();
                } catch (JSONException e) {
                    Toast.makeText(ClientActivity.this, "Failed to parse data: " + e, Toast.LENGTH_SHORT).show();
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
                ((TextView) findViewById(R.id.tw_user)).setText(mUser.toString());
            }
        });
    }

    private void connectToRoom() {
        final TextView status = findViewById(R.id.tv_RoomId);
        final ServerHelper serverHelper = new ServerHelper();
        ServerRequest.Callback callback = new ServerRequest.Callback() {

            @Override
            public void execute(String response) {
                ServerRequest.Callback insideCallback = new ServerRequest.Callback() {
                    @Override
                    public void execute(String response) {
                        if(response != null) {
                            if(response.equals(ServerHelper.CONNECTION_ERROR) || response.equals(ServerHelper.RESPONSE_ERROR)){
                                status.setText(response);
                            }

                            try {
                                JSONObject roomInfo = new JSONObject(response.toString());
                                int roomId = roomInfo.getInt("id");
                                int loginCode = roomInfo.getInt("logincode");

                                mRoom.setId(roomId);
                                mRoom.setLoginCode(loginCode);

                                status.setText("Login code is " + String.valueOf(mRoom.getLoginCode()));
                            } catch (JSONException e) {
                                Log.d("MusicDJ", response.toString());
                                status.setText("Not connected");
                                e.printStackTrace();
                            }
                        }
                    }
                };
                serverHelper.connectToRoom(mRoom.getLoginCode(), mUser.getEmail(), insideCallback);
            }
        };
        serverHelper.connectUser(mUser.getEmail(), callback);
    }
}
