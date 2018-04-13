package com.tlopez.utils.timemarker.cellrenderers;

import com.tlopez.utils.timemarker.MarkedTime;
import com.tlopez.utils.timemarker.TimeMarkerUtils;
import javafx.scene.control.TableCell;
import javafx.scene.text.TextAlignment;

import java.time.Duration;

public class TimeCell extends TableCell<MarkedTime, Duration> {
    @Override
    protected void updateItem(Duration item, boolean empty) {
        super.updateItem(item, empty);

        if (item == null || empty) {
            setText(null);
            setStyle("");
        } else {
            setTextAlignment(TextAlignment.RIGHT);
            setText(TimeMarkerUtils.makeDurationString(item, true));
        }
    }
}
