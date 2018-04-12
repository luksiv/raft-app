package com.latenightpenguin.groupdj;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.latenightpenguin.groupdj.NetworkServices.ServerHelper;
import com.latenightpenguin.groupdj.NetworkServices.ServerRequest;
import com.latenightpenguin.groupdj.NetworkServices.SpotifyAPI.SpotifyData;
import com.latenightpenguin.groupdj.NetworkServices.SpotifyAPI.WrappedSpotifyCallback;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import kaaes.spotify.webapi.android.models.TracksPager;
import kaaes.spotify.webapi.android.models.UserPrivate;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class ClientActivity extends AppCompatActivity {

    //region Constants
    private static final String TAG = "ClientActivity";
    private static final String CLIENT_ID = "1b02f619aa8142db8cd6d3d9bc3d505e";
    private static final String REDIRECT_URI = "lnpapp://callback";
    private static final int AUTH_CODE = 1337;
    //endregion

    //region Fields
    private String mAccessToken;
    private User mUser;
    private RoomInfo mRoom;
    private ArrayList<String> mSongs;
    private OkHttpClient mOkHttpClient = new OkHttpClient();
    private Call mCall;
    private PlaylistArrayAdapter mPlaylistAdapter;
    private SpotifyData mSpotifyData;
    //endregion

    //region UI elements
    private Button btnAdd;
    private Button btnInfo;
    private Button btnToggleViews;
    private Button btnRefreshPlaylist;
    private ListView lwPlaylist;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ErrorHandler());
        setContentView(R.layout.activity_client);
        ErrorHandler.setContext(ClientActivity.this);

        mRoom = new RoomInfo();
        mRoom.setLoginCode(getIntent().getIntExtra("roomId", -1));

        authentication();
        setUpElements();


    }

    //region Methods that onCreate uses
    private void setUpElements() {
        btnAdd = findViewById(R.id.btn_AddSong);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ClientActivity.this,
                        AddSongActivity.class);
                intent.putExtra("accessToken", mAccessToken);
                startActivityForResult(intent, 333);
            }
        });

        btnToggleViews = findViewById(R.id.btn_toggleView);
        btnToggleViews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeViews(btnToggleViews);
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
            }
        });

        lwPlaylist = findViewById(R.id.lw_playlist);
        mPlaylistAdapter = new PlaylistArrayAdapter(this, new ArrayList<SongItem>());
        lwPlaylist.setAdapter(mPlaylistAdapter);


    }
    private void authentication() {
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-email"});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, AUTH_CODE, request);
    }
    //endregion

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == AUTH_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                mAccessToken = response.getAccessToken();
                mSpotifyData = new SpotifyData(mAccessToken);
                getUserInfo();
            }
            if (response.getType() == AuthenticationResponse.Type.ERROR) {
                Log.e("Authentification", response.getError());
            }
        }

        // AddSongActivity result
        if (requestCode == 333) {
            if (resultCode == AddSongActivity.RESULT_OK) {
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

    private void getUserInfo() {
        mSpotifyData.getUser(new WrappedSpotifyCallback<UserPrivate>(){
            @Override
            public void success(UserPrivate userPrivate, retrofit.client.Response response) {
                mUser = new User(userPrivate.id, userPrivate.display_name,
                        userPrivate.email, userPrivate.country);
                connectToRoom();
            }
        });
    }

    public void showInfoRoom() {
        final AlertDialog.Builder mBuilder = new AlertDialog.Builder(ClientActivity.this);
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

    private void updatePlaylist(){
        final ServerHelper serverHelper = new ServerHelper();
        ServerRequest.Callback getSongsCallback = new ServerRequest.Callback() {
            @Override
            public void execute(String response) {
                mSongs = serverHelper.convertToList(response);
                for (String song : mSongs) {
                    Log.d(TAG, song);
                }
                updatePlaylistView();
            }
        };
        serverHelper.getSongs(mRoom, getSongsCallback);
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

    private void changeViews(Button button) {
        LinearLayout player = findViewById(R.id.root_player);
        LinearLayout playlist = findViewById(R.id.root_playlist);

        if (player.getVisibility() == View.VISIBLE) {
            player.setVisibility(View.INVISIBLE);
            playlist.setVisibility(View.VISIBLE);
            button.setText("Show player");
        } else {
            player.setVisibility(View.VISIBLE);
            playlist.setVisibility(View.INVISIBLE);
            button.setText("Show playlist");
        }

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
                        if (response != null) {
                            if (response.equals(ServerHelper.CONNECTION_ERROR) || response.equals(ServerHelper.RESPONSE_ERROR)) {
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
