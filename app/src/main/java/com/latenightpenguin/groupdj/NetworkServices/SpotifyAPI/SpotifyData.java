package com.latenightpenguin.groupdj.NetworkServices.SpotifyAPI;

import android.util.Log;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Recommendations;
import kaaes.spotify.webapi.android.models.SavedTrack;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import kaaes.spotify.webapi.android.models.TracksPager;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.RetrofitError;
import retrofit.http.QueryMap;

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
            searchTracks(query, new WrappedSpotifyCallback<TracksPager>());
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

    static public ArrayList<WrappedTrack> ConvertTracksFromPager(TracksPager pager) {
        ArrayList<WrappedTrack> wrappedTracks = new ArrayList<>();

        for (Track track : pager.tracks.items) {
            wrappedTracks.add(new WrappedTrack(track));
        }

        return wrappedTracks;
    }

    public void getTrack(String id) {
        try {
            getTrack(id, new WrappedSpotifyCallback<Track>());
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
            getTracks(ids, new WrappedSpotifyCallback<Tracks>());
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
           getTracks(formatedIds, cb);
        } catch (RetrofitError er) {
            SpotifyError spotifyError = SpotifyError.fromRetrofitError(er);
            Log.v(TAG, spotifyError.getMessage());
        } catch (Exception ex) {
            Log.v(TAG, ex.getMessage());
        }
    }

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
            getUser(new WrappedSpotifyCallback<UserPrivate>());
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
            getUserTracks(new WrappedSpotifyCallback<Pager<SavedTrack>>());
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

    public void getRecomendationList(String ids)
    {
        try {
                getRecomendationList(ids, new WrappedSpotifyCallback<Recommendations>());
        } catch (RetrofitError er) {
            SpotifyError spotifyError = SpotifyError.fromRetrofitError(er);
            Log.v(TAG, spotifyError.getMessage());
        } catch (Exception ex) {
            Log.v(TAG, ex.getMessage());
        }
    }

    public void getRecomendationList(String ids, WrappedSpotifyCallback<Recommendations> cb)
    {
        try {
            getRecomendationList(ids, 5, cb);
        } catch (RetrofitError er) {
            SpotifyError spotifyError = SpotifyError.fromRetrofitError(er);
            Log.v(TAG, spotifyError.getMessage());
        } catch (Exception ex) {
            Log.v(TAG, ex.getMessage());
        }
    }

    public void getRecomendationList(String ids, int limit , WrappedSpotifyCallback<Recommendations> cb)
    {
        HashMap<String, Object> map = new HashMap<>();
        map.put("limit", limit);
        map.put("seed_tracks", ids);
        try {
            services.getRecommendations(map, cb);
        } catch (RetrofitError er) {
            SpotifyError spotifyError = SpotifyError.fromRetrofitError(er);
            Log.v(TAG, spotifyError.getMessage());
        } catch (Exception ex) {
            Log.v(TAG, ex.getMessage());
        }
    }

    static public ArrayList<WrappedTrack> ConvertRecomendedTracks(Recommendations recommendations) {
        ArrayList<WrappedTrack> tracks = new ArrayList<>();

        for (Track track : recommendations.tracks) {
            tracks.add(new WrappedTrack(track));
        }

        return tracks;
    }

    static public WrappedTrack WrapTrack(Track track) {
        return new WrappedTrack(track);
    }
}
