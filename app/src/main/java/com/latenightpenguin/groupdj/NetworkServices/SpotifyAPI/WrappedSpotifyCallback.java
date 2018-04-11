package com.latenightpenguin.groupdj.NetworkServices.SpotifyAPI;
import android.util.Log;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;

public class WrappedSpotifyCallback<T> extends SpotifyCallback<T> {
    @Override
    public void failure(SpotifyError spotifyError) {
        Log.v("WraperCallback", "Fail");
    }

    @Override
    public void success(T t, retrofit.client.Response response) {
        Log.v("WraperCallback", "Success");
    }
}
