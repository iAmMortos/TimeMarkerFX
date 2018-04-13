package com.tlopez.utils.timemarker.exporters;

import com.tlopez.utils.timemarker.MarkedTime;
import com.tlopez.utils.timemarker.MarkerType;
import com.tlopez.utils.timemarker.TimeMarkerUtils;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class TextExporter extends AbstractExporter {
    @Override
    public String getExportedText(List<MarkedTime> markedTimes) {
        MarkedTime firstTime = markedTimes.get(0);
        MarkedTime lastTime = markedTimes.get(markedTimes.size() - 1);

        StringBuilder sb = new StringBuilder();

        sb.append("Time Marker Log").append(NL);
        sb.append("Started: ").append(new SimpleDateFormat().format(Date.from(firstTime.getTimestamp()))).append(NL);
        sb.append("===================================");

        for (MarkedTime markedTime : markedTimes) {
            switch (markedTime.getType()) {
                case START:
                    sb.append(NL).append(" +[");
                    break;
                case POINT:
                    sb.append("  [");
                    break;
                case STOP:
                    sb.append(" -[");
                    break;
            }
            sb.append(markedTime.getType().getName()).append("]\t");
            sb.append(TimeMarkerUtils.makeDurationString(markedTime.getTime(), false));
            sb.append("\t").append(markedTime.getNotes()).append(NL);
        }

        List<MarkedTime> startsAndStops = markedTimes.stream()
                .filter(mt -> mt.getType() == MarkerType.STOP || mt.getType() == MarkerType.START)
                .collect(Collectors.toList());
        Duration usefulTime = Duration.ofNanos(0);
        Instant lastStart = startsAndStops.get(0).getTimestamp();
        for (MarkedTime markedTime : startsAndStops) {
            if (markedTime.getType() == MarkerType.START) {
                lastStart = markedTime.getTimestamp();
            } else {
                usefulTime = usefulTime.plus(Duration.between(lastStart, markedTime.getTimestamp()));
            }
        }

        sb.append("Total Time: ");
        sb.append(TimeMarkerUtils.makeDurationString(lastTime.getTime(), true)).append(NL);
        sb.append("Useful Time: ").append(TimeMarkerUtils.makeDurationString(usefulTime, true)).append(NL);
        sb.append("Finished: ").append(new SimpleDateFormat().format(Date.from(lastTime.getTimestamp())));

        return sb.toString();
    }
}
