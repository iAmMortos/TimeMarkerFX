package com.tlopez.utils.timemarker;

import com.tlopez.utils.timemarker.cellrenderers.TimeCell;
import com.tlopez.utils.timemarker.cellrenderers.TypeCell;
import com.tlopez.utils.timemarker.exporters.AbstractExporter;
import com.tlopez.utils.timemarker.exporters.ExportType;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

public class TimeMarkerController implements NativeKeyListener {

    private static final String VERSION = "1.0";
    private static final String FILE_FILTER = "fileFilter";
    private static final Logger LOGGER = Logger.getLogger(TimeMarkerController.class.getSimpleName());
    private final Preferences prefs = Preferences.userRoot().node(TimeMarkerController.class.getName());
    private final FileChooser fileChooser = new FileChooser();
    private SimpleBooleanProperty hasBeenStarted = new SimpleBooleanProperty(false);
    private SimpleBooleanProperty isGoodTime = new SimpleBooleanProperty(false);
    private Instant startTime = null;
    private Instant lastStop = null;
    private boolean isExported = true;
    private ObservableList<MarkedTime> markedTimes = FXCollections.observableArrayList();
    private AbstractExporter exporter;
    private final Image astonishedFace =
            new Image(TimeMarkerApp.class.getResourceAsStream(
                    "astonished-face_1f632 (m).png"));
    private final Image boredFace =
            new Image(TimeMarkerApp.class.getResourceAsStream(
                    "sleeping-face_1f634 (m).png"));
    private Duration timePaused;
    private Window parent;

    Thread timerUpdateThread;

    @FXML
    public GridPane statusPane;
    @FXML
    public ImageView statusIcon;
    @FXML
    public Label statusLabel;
    @FXML
    public Text goodTimeText;
    @FXML
    public Text totalTimeText;
    @FXML
    public Button startStopButton;
    @FXML
    public Button markButton;
    @FXML
    public Button resetButton;
    @FXML
    public TableView<MarkedTime> timesTable;
    @FXML
    public TableColumn<MarkedTime, MarkerType> typeColumn;
    @FXML
    public TableColumn<MarkedTime, Duration> timeColumn;
    @FXML
    public TableColumn<MarkedTime, String> notesColumn;
    @FXML
    public Hyperlink exportLink;
    @FXML
    public Label versionLabel;

    @FXML
    private void initialize() {
        timePaused = Duration.ofNanos(0);

        fileChooser.setTitle("Select Export Location");
        fileChooser.getExtensionFilters().addAll(ExportType.getExtensionFilterList());
        String exportTypeFromPrefs = prefs.get(FILE_FILTER, ExportType.TextFile.toString());
        ExportType exportType = ExportType.valueOf(exportTypeFromPrefs);
        fileChooser.setSelectedExtensionFilter(exportType.getExtensionFilter());

        timesTable.setItems(markedTimes);
        timesTable.setPlaceholder(new Text("Press START to begin logging times."));
        statusIcon.imageProperty().bind(Bindings.when(isGoodTime).then(astonishedFace).otherwise(boredFace));
        statusLabel.textProperty().bind(Bindings.when(isGoodTime).then("INTERESTING!!1").otherwise("boring..."));
        statusPane.styleProperty().bind(Bindings.when(isGoodTime).then("-fx-background-color: #bced80").otherwise("-fx-background-color: #ed8080"));

        startStopButton.textProperty().bind(Bindings.when(isGoodTime).then("Pause").otherwise("Start"));
        startStopButton.setMaxHeight(Double.POSITIVE_INFINITY);
        startStopButton.setMaxWidth(Double.POSITIVE_INFINITY);
        GridPane.setFillHeight(startStopButton, true);
        GridPane.setFillWidth(startStopButton, true);
        markButton.disableProperty().bind(hasBeenStarted.not());

        timesTable.setEditable(true);
        timesTable.getItems().addListener((ListChangeListener<MarkedTime>) c ->
                Platform.runLater(() -> timesTable.scrollTo(c.getList().size() - 1)));

        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeColumn.setCellFactory(column -> new TypeCell());

        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        timeColumn.setCellFactory(column -> new TimeCell());

        notesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));
        notesColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        notesColumn.setOnEditCommit(
                event -> event.getTableView().getItems().get(
                        event.getTablePosition().getRow()
                ).setNotes(event.getNewValue()));

        exportLink.disableProperty().bind(hasBeenStarted.not());
        versionLabel.setText("Version " + VERSION);

        // Initialize native hooks
        Logger.getLogger(GlobalScreen.class.getPackage().getName()).setLevel(Level.OFF);
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
        } catch (NativeHookException ex) {
            LOGGER.severe("There was a problem registering the native hook.");
            LOGGER.severe(ex.getMessage());
        }

    }

    @FXML
    private void onStartStop() {

        if (!hasBeenStarted.getValue()) {
            isExported = false;
            startTime = new Date().toInstant();
            markedTimes.add(new MarkedTime(MarkerType.START, startTime));
            timerUpdateThread = new AppUpdateThread();
            Platform.runLater(() ->
            {
                hasBeenStarted.setValue(true);
                isGoodTime.setValue(true);
                timerUpdateThread.start();
            });
        } else {
            Platform.runLater(() ->
            {
                isGoodTime.setValue(!isGoodTime.getValue());
                if (isGoodTime.getValue()) {
                    MarkedTime markedTime = new MarkedTime(MarkerType.START, startTime);
                    timePaused = timePaused.plus(Duration.between(lastStop, markedTime.getTimestamp()));
                    markedTimes.add(markedTime);
                } else {
                    MarkedTime markedTime = new MarkedTime(MarkerType.STOP, startTime);
                    lastStop = markedTime.getTimestamp();
                    markedTimes.add(markedTime);
                }
            });
        }
    }

    @FXML
    private void onMark() {
        if (hasBeenStarted.getValue()) {
            markedTimes.add(new MarkedTime(MarkerType.POINT, startTime));
        }
    }

    @FXML
    private void onReset() {
        if (verifyClose()) {
            Platform.runLater(
                    () ->
                    {
                        startTime = null;
                        if (timerUpdateThread != null) timerUpdateThread.interrupt();
                        totalTimeText.setText(TimeMarkerUtils.makeDurationString(Duration.ofNanos(0), false));
                        goodTimeText.setText(TimeMarkerUtils.makeDurationString(Duration.ofNanos(0), false));
                        hasBeenStarted.setValue(false);
                        isGoodTime.setValue(false);
                        markedTimes.clear();
                        timePaused = Duration.ofNanos(0);
                    });
        }
    }

    @FXML
    private void onExport() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "This will end the current session and all timers will be reset to 0. Proceed?",
                ButtonType.YES,
                ButtonType.NO,
                ButtonType.CANCEL);
        alert.showAndWait();
        if (ButtonType.YES == alert.getResult()) {
            File file = fileChooser.showSaveDialog(this.parent);
            if (file != null) {
                FileChooser.ExtensionFilter selectedFilter = fileChooser.getSelectedExtensionFilter();
                ExportType selectedExportType = ExportType.getExportTypeFromExtensionFilter(selectedFilter);
                AbstractExporter exporter = ExportType.getExporterFromExtensionFilter(selectedFilter);
                prefs.put(FILE_FILTER, selectedExportType.toString());
                try {
                    MarkedTime finalStop = null;
                    if (isGoodTime.getValue()) {
                        finalStop = new MarkedTime(MarkerType.STOP, startTime);
                    }
                    FileWriter fileWriter = new FileWriter(file);

                    fileWriter.write(getExportText(finalStop, exporter));
                    fileWriter.close();
                    isExported = true;
                    onReset();
                } catch (IOException ex) {
                    LOGGER.severe("Unable to export file to specified location.");
                    LOGGER.severe(ex.getMessage());
                }
            }
        }
    }

    private String getExportText(MarkedTime finalStop, AbstractExporter exporter) {
        if (finalStop != null) {
            markedTimes.add(finalStop);
        }

        return exporter.getExportedText(markedTimes);
    }

    private String getExportText(AbstractExporter exporter) {
        return getExportText(null, exporter);
    }

    private class AppUpdateThread extends Thread {
        public AppUpdateThread() {
            super();
            setDaemon(true);
        }

        @Override
        public void run() {
            boolean running = true;
            while (running) {

                Instant now = new Date().toInstant();
                totalTimeText.setText(
                        TimeMarkerUtils.makeDurationString(
                                Duration.between(startTime, now),
                                false));

                if (isGoodTime.get()) {
                    goodTimeText.setText(
                            TimeMarkerUtils.makeDurationString(
                                    Duration.between(startTime, now.minus(timePaused)),
                                    false));
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    running = false;
                }
            }
        }
    }

    protected void cleanUp() {
        try {
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException ex) {
            ex.printStackTrace();
        }
    }

    public boolean verifyClose() {
        if (hasBeenStarted.getValue() && !isExported) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "You have un-exported time markers, are you sure you wish to proceed?",
                    ButtonType.YES,
                    ButtonType.NO,
                    ButtonType.CANCEL);
            alert.showAndWait();
            return ButtonType.YES == alert.getResult();
        }

        return true;
    }

    public void setParent(Window window) {
        this.parent = window;
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeEvent) {
        // Pause Break
        if (nativeEvent.getKeyCode() == NativeKeyEvent.VC_PAUSE) {
            onStartStop();
        }
        // Home
        else if (nativeEvent.getKeyCode() == NativeKeyEvent.VC_HOME) {
            onMark();
        }
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeEvent) { /* Take no action */ }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeEvent) { /* Take no action */ }
}
