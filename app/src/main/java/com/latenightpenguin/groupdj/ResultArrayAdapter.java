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
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class ResultArrayAdapter extends ArrayAdapter<SongItem> {

    public ResultArrayAdapter(Context context, ArrayList<SongItem> list) {
        super(context, 0, list);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final SongItem item = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_result_song, parent, false);
        }

        TextView song = convertView.findViewById(R.id.item_songName);
        TextView artists = convertView.findViewById(R.id.item_artist);

        song.setText(item.getmSongName());
        artists.setText(item.getmArtists());

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent data = new Intent();
                data.putExtra("uri", item.getmUri());
                ((AddSongActivity) getContext()).setResult(Activity.RESULT_OK, data);
                ((AddSongActivity) getContext()).finish();
            }
        });

        return convertView;
    }
}
