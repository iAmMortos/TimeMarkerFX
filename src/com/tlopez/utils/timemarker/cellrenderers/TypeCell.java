package com.tlopez.utils.timemarker.cellrenderers;

import com.tlopez.utils.timemarker.MarkedTime;
import com.tlopez.utils.timemarker.MarkerType;
import javafx.scene.control.TableCell;

public class TypeCell extends TableCell<MarkedTime, MarkerType> {
    @Override
    protected void updateItem(MarkerType item, boolean empty) {
        super.updateItem(item, empty);

        if (item == null || empty) {
            setText(null);
            setStyle("");
            getStyleClass().clear();
        } else {
            setText(item.getName());
            getStyleClass().clear();

            switch (item) {
                case START:
                    getStyleClass().add("startTime");
                    break;
                case STOP:
                    getStyleClass().add("stopTime");
                    break;
                case POINT:
                    getStyleClass().add("pointTime");
                    break;
            }
        }
    }
}
