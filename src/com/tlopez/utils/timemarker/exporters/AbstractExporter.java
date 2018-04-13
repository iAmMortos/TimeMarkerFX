package com.tlopez.utils.timemarker.exporters;

import com.tlopez.utils.timemarker.MarkedTime;

import java.util.List;

public abstract class AbstractExporter {
    protected static final String NL = System.lineSeparator();

    public abstract String getExportedText(List<MarkedTime> markedTimes);
}
