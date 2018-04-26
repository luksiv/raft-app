package com.latenightpenguin.groupdj.NetworkServices.ServerAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SongConverter {
    public static String getSongId(String response) {
        String songId = "";

        if (response != null && !response.equals("")) {
            try {
                JSONObject songObject = new JSONObject(response);

                songId = songObject.getString("song");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return songId;
    }

    public static ArrayList<String> convertToList(String response) {
        ArrayList<String> songs = new ArrayList<>();

        if (response != null && !response.equals("")) {
            try {
                JSONArray array = new JSONArray(response);

                for (int i = 0; i < array.length(); i++) {
                    JSONObject song = array.getJSONObject(i);
                    songs.add(song.getString("song"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return songs;
    }
}
