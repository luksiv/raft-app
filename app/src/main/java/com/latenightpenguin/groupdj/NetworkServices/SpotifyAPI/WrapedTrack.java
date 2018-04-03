package com.latenightpenguin.groupdj.NetworkServices.SpotifyAPI;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;

public class WrapedTrack {

    Track track;

    public  WrapedTrack(Track track)
    {
        this.track = track;
    }

    public String getUri()
    {
        return track.uri;
    }

    // TODO issiaiskinti kuris yra kuris
    public String[]  getAlbumArtUrl()
    {
        String[] url = new String[3];
        url[0] = track.album.images.get(0).url;
        url[1] = track.album.images.get(1).url;
        url[2] = track.album.images.get(2).url;

        return url;
    }

    public long getDuration()
    {
        return track.duration_ms;
    }
    public  String getId()
    {
        return track.id;
    }
    public  String getName()
    {
        return track.name;
    }
    public ArrayList<String> getArtist() {
        ArrayList<String> list = new ArrayList<String>();

        for (ArtistSimple artist : track.artists) {
            list.add(artist.name);
        }

        return list;
    }

}
