package com.lertos.spriteeditor;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    private static final double START_SCREEN_WIDTH = 1280;
    private static final double START_SCREEN_HEIGHT = 800;
    private static final String CSS_STYLE_SHEET = "/com/lertos/spriteeditor/styles.css";
    private static final String TITLE_WINDOW = "Sprite Sheet Tagger";
    private static final String TITLE_EDITOR = "Editor";
    private static final String TITLE_RESULTS = "Results";

    @Override
    public void start(Stage primaryStage) {
        AppModel model = new AppModel();

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        EditorTab editorTab = new EditorTab(model);
        ResultsTab resultsTab = new ResultsTab(model);

        Tab tab1 = new Tab(TITLE_EDITOR, editorTab.getRoot());
        Tab tab2 = new Tab(TITLE_RESULTS, resultsTab.getRoot());

        tabPane.getTabs().addAll(tab1, tab2);

        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == tab2) {
                resultsTab.refresh();
            }
        });

        Scene scene = new Scene(tabPane, START_SCREEN_WIDTH, START_SCREEN_HEIGHT);
        scene.getStylesheets().add(getClass().getResource(CSS_STYLE_SHEET).toExternalForm());

        primaryStage.setTitle(TITLE_WINDOW);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
