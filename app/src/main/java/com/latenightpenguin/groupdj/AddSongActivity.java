package com.latenightpenguin.groupdj;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

class SongItem {

    String mSongName;
    String mArtists;
    String mAlbum;
    String mUri;

    public SongItem(String songName, String artists, String album, String uri) {
        mSongName = songName;
        mArtists = artists;
        mAlbum = album;
        mUri = uri;
    }

    public String getmSongName() {
        return mSongName;
    }

    public String getmArtists() {
        return mArtists;
    }

    public String getmAlbum() {
        return mAlbum;
    }

    public String getmUri() {
        return mUri;
    }
}

public class AddSongActivity extends AppCompatActivity {

    private OkHttpClient mOkHttpClient = new OkHttpClient();
    private Call mCall;

    String mAccessToken;
    Timer timer;

    EditText et_input;
    ImageView ib_testButton;
    ListView lw_searchResults;

    ResultArrayAdapter adapter;
    ArrayList<SongItem> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        ib_testButton = findViewById(R.id.ib_searchTestButton);
        lw_searchResults = findViewById(R.id.lw_searchResults);

        list = new ArrayList<>();
        adapter = new ResultArrayAdapter(this, list);
        lw_searchResults.setAdapter(adapter);

        ib_testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et_input.getText().toString().length() > 0) {
                    getTracks();
                }
            }
        });
    }

    //TODO: Remove this method when wrapper is working correctly
    public void getTracks() {

        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/search?q=" + et_input.getText().toString() + "&type=track&limit=15")
                .addHeader("Authorization", "Bearer " + mAccessToken)
                .build();


        cancelCall();
        mCall = mOkHttpClient.newCall(request);

        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(AddSongActivity.this, "Failed to fetch data: " + e, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    final ArrayList<SongItem> results = new ArrayList<>();
                    JSONObject jsonObject = new JSONObject(response.body().string()).getJSONObject("tracks");
                    int total = jsonObject.getInt("total");
                    int limit = jsonObject.getInt("limit");
                    JSONArray jsonArray = jsonObject.getJSONArray("items");
                    if (total == 0) {
                        //TODO: implement null state
                    }
                    JSONObject obj;
                    if (total >= limit) {
                        for (int i = 0; i < limit; i++) {
                            obj = jsonArray.getJSONObject(i);
                            String song = obj.getString("name");
                            String artist = obj.getJSONArray("artists").getJSONObject(0).getString("name");
                            String album = obj.getJSONObject("album").getString("name");
                            String uri = obj.getString("uri");
                            results.add(new SongItem(song, artist, album, uri));
                            String ret = String.format("Album: '%s', Artist '%s', Song '%s'", album, artist, song);
                            Log.e("AddSongActivity", ret);
                        }
                    } else {
                        for (int i = 0; i < total; i++) {
                            obj = jsonArray.getJSONObject(i);
                            String song = obj.getString("name");
                            String artist = obj.getJSONArray("artists").getJSONObject(0).getString("name");
                            String album = obj.getJSONObject("album").getString("name");
                            String uri = obj.getString("uri");
                            results.add(new SongItem(song, artist, album, uri));
                            String ret = String.format("Album: '%s', Artist '%s', Song '%s'", album, artist, song);
                            Log.e("AddSongActivity", ret);
                        }
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.clear();
                            adapter.addAll(results);
                        }
                    });


                } catch (Exception e) {
                    Log.e("AddSongActivity", "no gucci");
                    Log.e("AddSongActivity", e.getMessage());
                }

            }
        });
    }

    private void cancelCall() {
        if (mCall != null) {
            mCall.cancel();
        }
    }
}
