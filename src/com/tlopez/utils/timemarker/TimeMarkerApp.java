package com.tlopez.utils.timemarker;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TimeMarkerApp extends Application {

    public static final String TITLE = "T-Lo's Dope Time Marker";
    private TimeMarkerController controller;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("TimeMarker.fxml").openStream());
        root.getStylesheets().add("com/tlopez/utils/timemarker/style.css");
        controller = fxmlLoader.getController();
        controller.setParent(primaryStage);
        primaryStage.setTitle(TITLE);
        primaryStage.setScene(new Scene(root));
        primaryStage.setOnCloseRequest(
                windowEvent -> {
                    if (controller.verifyClose()) {
                        controller.cleanUp();
                    } else {
                        windowEvent.consume();
                    }
                });
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
