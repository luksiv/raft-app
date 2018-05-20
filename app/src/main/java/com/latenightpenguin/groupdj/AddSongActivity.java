package com.latenightpenguin.groupdj;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ListView;

import com.latenightpenguin.groupdj.Models.SongItem;
import com.latenightpenguin.groupdj.NetworkServices.SpotifyAPI.SpotifyData;
import com.latenightpenguin.groupdj.NetworkServices.SpotifyAPI.WrappedSpotifyCallback;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import okhttp3.Call;
import okhttp3.OkHttpClient;

public class AddSongActivity extends AppCompatActivity {

    private OkHttpClient mOkHttpClient = new OkHttpClient();
    private Call mCall;

    String mAccessToken;
    Timer timer;

    EditText et_input;
    ListView lw_searchResults;

    ResultArrayAdapter adapter;
    ArrayList<SongItem> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ErrorHandler());
        ErrorHandler.setContext(AddSongActivity.this);
        ErrorHandler.setView(findViewById(R.id.root_addsongactivity));
        setContentView(R.layout.activity_add_song);


        mAccessToken = getIntent().getStringExtra("accessToken");

        et_input = findViewById(R.id.et_searchInput);
        et_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (timer != null) {
                    timer.cancel();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        getTracks();
                    }
                }, 750); // 750ms delay before the timer executes the „run“ method from TimerTask

            }
        });
        lw_searchResults = findViewById(R.id.lw_searchResults);

        list = new ArrayList<>();
        adapter = new ResultArrayAdapter(this, list);
        lw_searchResults.setAdapter(adapter);
    }

    public void getTracks() {
        SpotifyData spotifyData = new SpotifyData(mAccessToken);


        spotifyData.searchTracks(et_input.getText().toString(), new WrappedSpotifyCallback<TracksPager>() {
            @Override
            public void success(TracksPager tracksPager, retrofit.client.Response response) {
                final ArrayList<SongItem> results = new ArrayList<>();
                for (Track track : tracksPager.tracks.items) {
                    String song = track.name;
                    String artist = Utilities.convertArtistListToString(track.artists);
                    String album = track.album.name;
                    String uri = track.uri;
                    results.add(new SongItem(song, artist, album, uri));
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.clear();
                        adapter.addAll(results);
                    }
                });
            }
        });
    }

    private void cancelCall() {
        if (mCall != null) {
            mCall.cancel();
        }
    }
}
