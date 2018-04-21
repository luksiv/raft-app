package com.latenightpenguin.groupdj;

import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;


public class TracksRepository {
    static public LinkedList<String> lastPlayedTracks;
    static public LinkedList<String> generatedTracks;

    static public void setUp()
    {
        lastPlayedTracks  = new LinkedList<>();
        generatedTracks = new LinkedList<>();
        generatedTracks.addFirst("spotify:track:0d28khcov6AiegSCpG5TuT");
    }

    static public boolean isNOTLastAdded(String uri)
    {
        if(lastPlayedTracks.size() == 0 || !lastPlayedTracks.peekFirst().equals(uri))
        {
            return true;
        }

        return false;
    }

    static public ArrayList<String> toArray()
    {
        ArrayList<String> array = new ArrayList<>();
        array.addAll(TracksRepository.lastPlayedTracks);
        return array;
    }


    static public void addToLastPlayed(String uri)
    {
        lastPlayedTracks.addFirst(uri);
        if(lastPlayedTracks.size() > 5)
        {
            lastPlayedTracks.removeLast();
        }
    }

    static public void addToGeneratedTracks(String uri)
    {
        generatedTracks.addFirst(uri);
        if(generatedTracks.size() > 5)
        {
            generatedTracks.removeLast();
        }
    }

    static public String getFromGeneratedTracks()
    {
        if(!generatedTracks.isEmpty())
        {
            return generatedTracks.removeFirst();
        }

        return "spotify:track:4uLU6hMCjMI75M1A2tKUQC";
    }
}
