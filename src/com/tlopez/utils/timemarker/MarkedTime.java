package com.tlopez.utils.timemarker;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

public class MarkedTime {

    private Instant timestamp;
    private MarkerType type;
    private Duration time;
    private String notes;

    public MarkedTime(MarkerType type, Instant startTime) {
        Date now = new Date();
        this.timestamp = now.toInstant();
        this.type = type;
        this.time = Duration.between(startTime, timestamp);

        this.notes = this.time.getSeconds() == 0 ? ("Started " + new SimpleDateFormat().format(now)) : "";
    }

    public MarkerType getType() {
        return this.type;
    }

    public Duration getTime() {
        return this.time;
    }

    public String getNotes() {
        return this.notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getTimestamp() {
        return this.timestamp;
    }
}
