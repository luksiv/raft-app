package com.latenightpenguin.groupdj.Models;

public class SongItem {

    String mSongName;
    String mArtists;
    String mAlbum;
    String mUri;
    int mPosition;

    public SongItem(String songName, String artists, String album, String uri) {
        mSongName = songName;
        mArtists = artists;
        mAlbum = album;
        mUri = uri;
        mPosition = -1;
    }

    public SongItem(String songName, String artists, String album, String uri, int position) {
        mSongName = songName;
        mArtists = artists;
        mAlbum = album;
        mUri = uri;
        mPosition = position;
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

    public int getmPosition() {
        return mPosition;
    }


}