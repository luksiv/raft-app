package com.latenightpenguin.groupdj;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class PlaylistArrayAdapter extends ArrayAdapter<SongItem> {

    public PlaylistArrayAdapter(Context context, ArrayList<SongItem> list){
        super(context, 0, list);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final SongItem item = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_playlist_song, parent, false);
        }

        TextView pos = convertView.findViewById(R.id.item_position);
        TextView song = convertView.findViewById(R.id.item_songName);
        TextView artists = convertView.findViewById(R.id.item_artist);
        TextView album = convertView.findViewById(R.id.item_album);

        pos.setText(String.valueOf(position + 1) + ".");
        song.setText(item.getmSongName());
        artists.setText(item.getmArtists());
        album.setText(item.getmAlbum());

        return convertView;
    }
}
