package com.latenightpenguin.groupdj.NetworkServices.SpotifyAPI;

import android.util.Log;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.SavedTrack;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import kaaes.spotify.webapi.android.models.TracksPager;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.RetrofitError;

public class SpotifyData {
    private static final String TAG = "SpotifyData";

    SpotifyApi api;
    SpotifyService services;

    public SpotifyData(String token) {
        api = new SpotifyApi();
        api.setAccessToken(token);
        services = api.getService();
    }

    public void removeToken() {
        api.setAccessToken(null);
    }

    public void searchTracks(String query) {
        try {
            services.searchTracks(query, new WrappedSpotifyCallback<TracksPager>());
        } catch (RetrofitError er) {
            SpotifyError spotifyError = SpotifyError.fromRetrofitError(er);
            Log.v(TAG, spotifyError.getMessage());
        } catch (Exception ex) {
            Log.v(TAG, ex.getMessage());
        }
    }

    public void searchTracks(String query, WrappedSpotifyCallback<TracksPager> cb) {
        try {
            services.searchTracks(query, cb);
        } catch (RetrofitError er) {
            SpotifyError spotifyError = SpotifyError.fromRetrofitError(er);
            Log.v(TAG, spotifyError.getMessage());
        } catch (Exception ex) {
            Log.v(TAG, ex.getMessage());
        }
    }

    static public ArrayList<WrappedTrack> ConvertTracks(TracksPager pager) {
        ArrayList<WrappedTrack> wrappedTracks = new ArrayList<>();

        for (Track track : pager.tracks.items) {
            wrappedTracks.add(new WrappedTrack(track));
        }

        return wrappedTracks;
    }

    public void getTrack(String id) {
        try {
            services.getTrack(id, new WrappedSpotifyCallback<Track>());
        } catch (RetrofitError er) {
            SpotifyError spotifyError = SpotifyError.fromRetrofitError(er);
            Log.v(TAG, spotifyError.getMessage());
        } catch (Exception ex) {
            Log.v(TAG, ex.getMessage());
        }
    }

    public void getTrack(String id, WrappedSpotifyCallback<Track> cb) {
        try {
            services.getTrack(id, cb);
        } catch (RetrofitError er) {
            SpotifyError spotifyError = SpotifyError.fromRetrofitError(er);
            Log.v(TAG, spotifyError.getMessage());
        } catch (Exception ex) {
            Log.v(TAG, ex.getMessage());
        }
    }

    public void getTracks(String ids) {
        try {
            services.getTracks(ids, new WrappedSpotifyCallback<Tracks>());
        } catch (RetrofitError er) {
            SpotifyError spotifyError = SpotifyError.fromRetrofitError(er);
            Log.v(TAG, spotifyError.getMessage());
        } catch (Exception ex) {
            Log.v(TAG, ex.getMessage());
        }
    }

    public void getTracks(String ids, WrappedSpotifyCallback<Tracks> cb) {
        try {
            services.getTracks(ids, cb);
        } catch (RetrofitError er) {
            SpotifyError spotifyError = SpotifyError.fromRetrofitError(er);
            Log.v(TAG, spotifyError.getMessage());
        } catch (Exception ex) {
            Log.v(TAG, ex.getMessage());
        }
    }

    public void getTracks(ArrayList<String> ids, WrappedSpotifyCallback<Tracks> cb) {
        try {
            String formatedIds = convertArrayToString(ids);
            services.getTracks(formatedIds, cb);
        } catch (RetrofitError er) {
            SpotifyError spotifyError = SpotifyError.fromRetrofitError(er);
            Log.v(TAG, spotifyError.getMessage());
        } catch (Exception ex) {
            Log.v(TAG, ex.getMessage());
        }
    }

    /**
     * Converts from ArrayList<String> with entries like "spotify:track:xxxxxxxx" to string
     * like xxxxxxxxxx,xxxxxxxxxx,xxxxxxxx
     * @param array
     * @return
     */
    public String convertArrayToString(ArrayList<String> array) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.size() - 1; i++) {
            sb.append(array.get(i).split(":")[2]);
            sb.append(',');
        }

        sb.append(array.get(array.size() - 1).split(":")[2]);

        return sb.toString();
    }

    public void getUser() {
        try {
            services.getMe(new WrappedSpotifyCallback<UserPrivate>());
        } catch (RetrofitError er) {
            SpotifyError spotifyError = SpotifyError.fromRetrofitError(er);
            Log.v(TAG, spotifyError.getMessage());
        } catch (Exception ex) {
            Log.v(TAG, ex.getMessage());
        }
    }

    public void getUser(WrappedSpotifyCallback<UserPrivate> cb) {
        try {
            services.getMe(cb);
        } catch (RetrofitError er) {
            SpotifyError spotifyError = SpotifyError.fromRetrofitError(er);
            Log.v(TAG, spotifyError.getMessage());
        } catch (Exception ex) {
            Log.v(TAG, ex.getMessage());
        }
    }


    public void getUserTracks() {
        try {
            services.getMySavedTracks();
        } catch (RetrofitError er) {
            SpotifyError spotifyError = SpotifyError.fromRetrofitError(er);
            Log.v(TAG, spotifyError.getMessage());
        } catch (Exception ex) {
            Log.v(TAG, ex.getMessage());
        }
    }

    public void getUserTracks(WrappedSpotifyCallback<Pager<SavedTrack>> cb) {
        try {
            services.getMySavedTracks(cb);
        } catch (RetrofitError er) {
            SpotifyError spotifyError = SpotifyError.fromRetrofitError(er);
            Log.v(TAG, spotifyError.getMessage());
        } catch (Exception ex) {
            Log.v(TAG, ex.getMessage());
        }
    }

    static public ArrayList<WrappedTrack> ConvertUserTracks(Pager<SavedTrack> pager) {
        ArrayList<WrappedTrack> tracks = new ArrayList<>();

        for (SavedTrack track : pager.items) {
            tracks.add(new WrappedTrack(track.track));
        }

        return tracks;
    }

    static public WrappedTrack WrapTracks(Track track) {
        return new WrappedTrack(track);
    }
}