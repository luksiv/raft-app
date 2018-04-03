package com.latenightpenguin.groupdj.NetworkServices.SpotifyAPI;

import android.util.Log;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.SavedTrack;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.RetrofitError;

public class SpotifyData {
    SpotifyApi api;
    SpotifyService services;

    public SpotifyData(String token){
        api = new SpotifyApi();
        api.setAccessToken(token);
        services = api.getService();
    }

    private TracksPager searchTracksAPI(String query)
    {
        try
        {
            return services.searchTracks(query);
        }catch (RetrofitError er)
        {
            SpotifyError spotifyError = SpotifyError.fromRetrofitError(er);
        }catch (Exception ex)
        {

        }

        return null;
    }

    public ArrayList<WrapedTrack> searchTracks(String query)
    {
        ArrayList<WrapedTrack> wrapedTracks = new ArrayList<>();
        TracksPager pager = searchTracksAPI(query);

        for(Track track : pager.tracks.items)
        {
            wrapedTracks.add(new WrapedTrack(track));
        }

        return wrapedTracks;
    }

    public WrapedTrack getTrack(String id)
    {
        try
        {
            return new WrapedTrack(services.getTrack(id));
        }catch (RetrofitError er)
        {
            SpotifyError spotifyError = SpotifyError.fromRetrofitError(er);
        }catch (Exception ex)
        {

        }

        return null;
    }

    private UserPrivate getUser()
    {
        try
        {
            return services.getMe();
        }catch (RetrofitError er)
        {
            SpotifyError spotifyError = SpotifyError.fromRetrofitError(er);
        }catch (Exception ex)
        {

        }

        return null;
    }

    public String getUserId()
    {
        try
        {
            return getUser().id;
        }catch (RetrofitError er)
        {
            SpotifyError spotifyError = SpotifyError.fromRetrofitError(er);
        }catch (Exception ex)
        {

        }

        return null;
    }

    public String getUserEmail()
    {
        try
        {
            return getUser().email;
        }catch (RetrofitError er)
        {
            SpotifyError spotifyError = SpotifyError.fromRetrofitError(er);
        }catch (Exception ex)
        {

        }

        return null;
    }

    private Pager<SavedTrack> getUserTracksAPI()
    {
        try
        {
            return services.getMySavedTracks();
        }catch (RetrofitError er)
        {
            SpotifyError spotifyError = SpotifyError.fromRetrofitError(er);
        }catch (Exception ex)
        {

        }

        return null;
    }

    public ArrayList<WrapedTrack> getUserTracks()
    {
        ArrayList<WrapedTrack> tracks = new ArrayList<>();
        Pager<SavedTrack> pager = getUserTracksAPI();

        for(SavedTrack track : pager.items)
        {
            tracks.add(new WrapedTrack(track.track));
        }

        return tracks;
    }


}
