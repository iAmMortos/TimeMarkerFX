package com.tlopez.utils.timemarker;

import java.time.Duration;

public class TimeMarkerUtils {
    private TimeMarkerUtils() {
        // static utils class
    }

    static String frontFill(Object obj, long length, char fillChar) {
        String s = obj.toString();
        while (s.length() < length)
            s = fillChar + s;
        return s;
    }

    public static String makeDurationString(Duration duration, boolean trimFront) {
        long totalSeconds = duration.getSeconds();
        long hours = totalSeconds / 3600;
        totalSeconds %= 3600;
        long minutes = totalSeconds / 60;
        totalSeconds %= 60;
        long seconds = totalSeconds;
        // to the tenth of a second
        long milis = duration.getNano() / 100000000;
        return ((!trimFront || hours != 0) ? (hours + ":") : "") +
                ((!trimFront || hours != 0 || minutes != 0) ? (frontFill(minutes, 2, '0') + ":") : "") +
                frontFill(seconds, 2, '0') + "." +
                milis;
    }
}
