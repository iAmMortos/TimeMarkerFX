package com.tlopez.utils.timemarker;

public enum MarkerType {
    START("Start"),
    STOP("Stop"),
    POINT("Point");

    private String name;
    MarkerType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
