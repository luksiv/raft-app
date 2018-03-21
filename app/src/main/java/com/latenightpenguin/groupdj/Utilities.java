package com.latenightpenguin.groupdj;

/**
 * Created by Lukas Sivickas on 2018-03-21.
 */

import android.util.Log;

/**
 * This class serves as a place to put ulitity functions
 */
public class Utilities {

    /**
     * formats the input to a string ( Example: input = 1000; output = "0:01" )
     *
     * @param miliseconds
     * @return formatted string ( E.g. "10:01" or "1:10" etc )
     */
    public static String formatSeconds(long miliseconds) {
        long minutes = (miliseconds / 1000) / 60;
        long seconds = (miliseconds / 1000) % 60;
        String secondsString;
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }
        return String.format("%d:%s", minutes, secondsString);
    }

    /**
     * Function to get Progress percentage
     *
     * @param currentDuration
     * @param totalDuration
     */
    public static int getProgressPercentage(long currentDuration, long totalDuration) {
        Double percentage;

        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);

        // calculating percentage
        percentage = (((double) currentSeconds) / totalSeconds) * 100;

        // return percentage
        return percentage.intValue();
    }
}
