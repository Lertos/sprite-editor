package com.lertos.spriteeditor;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ResultsTab {

    private final AppModel model;
    private final BorderPane root;
    private final VBox categoriesContainer;

    public ResultsTab(AppModel model) {
        this.model = model;
        this.root = new BorderPane();
        this.categoriesContainer = new VBox(20);

        buildUI();
    }

    private void buildUI() {
        VBox topBar = new VBox(8);
        topBar.setPadding(new Insets(10, 12, 8, 12));
        topBar.setStyle("-fx-background-color: #3c3c3c; -fx-border-color: #555 transparent transparent transparent;");

        Label title = new Label("Results — Tagged Cells per Category");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #eeeeee;");

        Label hint = new Label("Switch to this tab to refresh. Each category shows the spritesheet with only its tagged cells highlighted.");
        hint.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 11px;");

        topBar.getChildren().addAll(title, hint);
        root.setTop(topBar);

        ScrollPane scroll = new ScrollPane(categoriesContainer);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #2b2b2b; -fx-background: #2b2b2b;");
        categoriesContainer.setPadding(new Insets(16));

        root.setCenter(scroll);
    }

    public void refresh() {
        categoriesContainer.getChildren().clear();

        if (model.getSpritesheet() == null) {
            Label empty = new Label("No spritesheet loaded. Go to the Editor tab to load an image.");
            empty.setStyle("-fx-text-fill: #888888; -fx-font-size: 13px;");
            categoriesContainer.getChildren().add(empty);
            return;
        }

        if (model.getCategories().isEmpty()) {
            Label empty = new Label("No categories defined. Go to the Editor tab to add categories.");
            empty.setStyle("-fx-text-fill: #888888; -fx-font-size: 13px;");
            categoriesContainer.getChildren().add(empty);
            return;
        }

        boolean anyAssigned = false;
        for (AppModel.Category cat : model.getCategories()) {
            List<AppModel.CellKey> cells = getCellsForCategory(cat);
            if (!cells.isEmpty()) {
                anyAssigned = true;
                categoriesContainer.getChildren().add(buildCategorySection(cat, cells));
            }
        }

        if (!anyAssigned) {
            Label empty = new Label("No cells have been tagged yet. Go to the Editor tab to tag cells.");
            empty.setStyle("-fx-text-fill: #888888; -fx-font-size: 13px;");
            categoriesContainer.getChildren().add(empty);
        }
    }

    private List<AppModel.CellKey> getCellsForCategory(AppModel.Category cat) {
        List<AppModel.CellKey> cells = new ArrayList<>();
        for (Map.Entry<AppModel.CellKey, AppModel.Category> entry : model.getCellAssignments().entrySet()) {
            if (cat.equals(entry.getValue())) {
                cells.add(entry.getKey());
            }
        }
        return cells;
    }

    private Node buildCategorySection(AppModel.Category cat, List<AppModel.CellKey> cells) {
        VBox section = new VBox(10);
        section.setStyle("-fx-background-color: #383838; -fx-background-radius: 8; -fx-border-color: #555; -fx-border-radius: 8; -fx-border-width: 1;");
        section.setPadding(new Insets(12));

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        javafx.scene.shape.Rectangle swatch = new javafx.scene.shape.Rectangle(20, 20);
        swatch.setArcWidth(4);
        swatch.setArcHeight(4);
        swatch.setFill(cat.getColor());
        swatch.setStroke(Color.gray(0.5));

        Label catName = new Label(cat.getName());
        catName.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #eeeeee;");

        Label cellCount = new Label("— " + cells.size() + " cell" + (cells.size() == 1 ? "" : "s") + " tagged");
        cellCount.setStyle("-fx-text-fill: #aaaaaa;");

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button saveBtn = new Button("Save Image...");
        saveBtn.setStyle("-fx-background-color: #5a7fc4; -fx-text-fill: white; -fx-font-weight: bold;");
        saveBtn.setOnAction(e -> saveImage(cat, cells));

        header.getChildren().addAll(swatch, catName, cellCount, spacer, saveBtn);

        Image preview = buildCategoryImage(cat, cells);
        ImageView imageView = new ImageView(preview);
        imageView.setPreserveRatio(true);

        double maxWidth = 900;
        double maxHeight = 500;
        double imgW = preview.getWidth();
        double imgH = preview.getHeight();

        double scale = Math.min(maxWidth / imgW, maxHeight / imgH);
        if (scale > 1.0) scale = 1.0;

        imageView.setFitWidth(imgW * scale);
        imageView.setFitHeight(imgH * scale);

        ScrollPane imgScroll = new ScrollPane(imageView);
        imgScroll.setFitToWidth(false);
        imgScroll.setFitToHeight(false);
        imgScroll.setStyle("-fx-background-color: #1e1e1e; -fx-background: #1e1e1e;");
        imgScroll.setPrefHeight(Math.min(imgH * scale + 30, maxHeight + 30));

        section.getChildren().addAll(header, imgScroll);
        return section;
    }

    private Image buildCategoryImage(AppModel.Category cat, List<AppModel.CellKey> cells) {
        Image spritesheet = model.getSpritesheet();
        int cellSize = model.getCellSize();
        int width = (int) spritesheet.getWidth();
        int height = (int) spritesheet.getHeight();

        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.drawImage(spritesheet, 0, 0);

        Color c = cat.getColor();
        gc.setFill(Color.color(c.getRed(), c.getGreen(), c.getBlue(), 0.45));
        gc.setStroke(Color.color(c.getRed(), c.getGreen(), c.getBlue(), 0.9));
        gc.setLineWidth(1.5);

        for (AppModel.CellKey key : cells) {
            double x = key.col() * cellSize;
            double y = key.row() * cellSize;
            gc.fillRect(x, y, cellSize, cellSize);
            gc.strokeRect(x + 0.75, y + 0.75, cellSize - 1.5, cellSize - 1.5);
        }

        gc.setStroke(Color.color(1, 1, 1, 0.4));
        gc.setLineWidth(0.5);
        for (double x = 0; x <= width; x += cellSize) {
            gc.strokeLine(x, 0, x, height);
        }
        for (double y = 0; y <= height; y += cellSize) {
            gc.strokeLine(0, y, width, y);
        }

        javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        return canvas.snapshot(params, null);
    }

    private void saveImage(AppModel.Category cat, List<AppModel.CellKey> cells) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save " + cat.getName() + " Image");
        fc.setInitialFileName(sanitizeFilename(cat.getName()) + "_tagged.png");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));

        File file = fc.showSaveDialog(root.getScene().getWindow());
        if (file == null) return;

        try {
            Image image = buildCategoryImage(cat, cells);
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);

            if (!file.getName().toLowerCase().endsWith(".png")) {
                file = new File(file.getAbsolutePath() + ".png");
            }

            ImageIO.write(bufferedImage, "png", file);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Saved");
            alert.setHeaderText(null);
            alert.setContentText("Saved to:\n" + file.getAbsolutePath());
            alert.showAndWait();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to save: " + ex.getMessage());
            alert.showAndWait();
        }
    }

    private String sanitizeFilename(String name) {
        return name.replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }

    public Node getRoot() {
        return root;
    }
}
