package com.latenightpenguin.groupdj;

public class SongItem {

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