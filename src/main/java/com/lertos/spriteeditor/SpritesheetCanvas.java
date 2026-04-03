package com.lertos.spriteeditor;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class SpritesheetCanvas {

    private final AppModel model;
    private final Canvas imageLayer;
    private final Canvas gridLayer;
    private final Canvas colorLayer;
    private final Pane container;

    private static final double GRID_OPACITY = 0.7;
    private static final double CELL_FILL_OPACITY = 0.45;

    public SpritesheetCanvas(AppModel model) {
        this.model = model;

        this.imageLayer = new Canvas(600, 400);
        this.gridLayer = new Canvas(600, 400);
        this.colorLayer = new Canvas(600, 400);

        this.container = new Pane(imageLayer, gridLayer, colorLayer);
        container.setMinSize(600, 400);

        colorLayer.setOnMouseClicked(this::handleCanvasClick);
        colorLayer.setOnMouseDragged(this::handleCanvasClick);

        model.selectedCategoryProperty().addListener((obs, old, cat) -> {
            colorLayer.setStyle(cat != null ? "-fx-cursor: crosshair;" : "-fx-cursor: default;");
        });
    }

    private void handleCanvasClick(javafx.scene.input.MouseEvent e) {
        if (model.getSpritesheet() == null) return;

        int cellSize = model.getCellSize();
        if (cellSize <= 0) return;

        int col = (int) (e.getX() / cellSize);
        int row = (int) (e.getY() / cellSize);

        int cols = model.getColumns();
        int rows = model.getRows();
        if (col < 0 || col >= cols || row < 0 || row >= rows) return;

        AppModel.Category current = model.getSelectedCategory();

        if (e.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
            model.assignCell(col, row, null);
        } else {
            model.assignCell(col, row, current);
        }

        redrawColorLayer();
    }

    public void redraw() {
        Image img = model.getSpritesheet();
        if (img == null) {
            resizeAll(600, 400);
            clearAll();
            return;
        }

        double w = img.getWidth();
        double h = img.getHeight();

        resizeAll(w, h);
        drawImageLayer(img, w, h);
        redrawGridLayer(w, h);
        redrawColorLayer();
    }

    private void resizeAll(double w, double h) {
        imageLayer.setWidth(w);
        imageLayer.setHeight(h);
        gridLayer.setWidth(w);
        gridLayer.setHeight(h);
        colorLayer.setWidth(w);
        colorLayer.setHeight(h);
        container.setMinSize(w, h);
        container.setPrefSize(w, h);
        container.setMaxSize(w, h);
    }

    private void clearAll() {
        imageLayer.getGraphicsContext2D().clearRect(0, 0, imageLayer.getWidth(), imageLayer.getHeight());
        gridLayer.getGraphicsContext2D().clearRect(0, 0, gridLayer.getWidth(), gridLayer.getHeight());
        colorLayer.getGraphicsContext2D().clearRect(0, 0, colorLayer.getWidth(), colorLayer.getHeight());
    }

    private void drawImageLayer(Image img, double w, double h) {
        GraphicsContext gc = imageLayer.getGraphicsContext2D();
        gc.clearRect(0, 0, w, h);
        gc.drawImage(img, 0, 0);
    }

    private void redrawGridLayer(double w, double h) {
        GraphicsContext gc = gridLayer.getGraphicsContext2D();
        gc.clearRect(0, 0, w, h);

        int cellSize = model.getCellSize();
        if (cellSize <= 0) return;

        gc.setStroke(Color.color(1, 1, 1, GRID_OPACITY));
        gc.setLineWidth(0.5);

        for (double x = 0; x <= w; x += cellSize) {
            gc.strokeLine(x, 0, x, h);
        }
        for (double y = 0; y <= h; y += cellSize) {
            gc.strokeLine(0, y, w, y);
        }
    }

    public void redrawColorLayer() {
        Image img = model.getSpritesheet();
        double w = img != null ? img.getWidth() : colorLayer.getWidth();
        double h = img != null ? img.getHeight() : colorLayer.getHeight();

        GraphicsContext gc = colorLayer.getGraphicsContext2D();
        gc.clearRect(0, 0, w, h);

        int cellSize = model.getCellSize();
        if (cellSize <= 0) return;

        for (var entry : model.getCellAssignments().entrySet()) {
            AppModel.CellKey key = entry.getKey();
            AppModel.Category cat = entry.getValue();
            if (cat == null) continue;

            Color c = cat.getColor();
            gc.setFill(Color.color(c.getRed(), c.getGreen(), c.getBlue(), CELL_FILL_OPACITY));
            gc.fillRect(key.col() * cellSize, key.row() * cellSize, cellSize, cellSize);

            gc.setStroke(Color.color(c.getRed(), c.getGreen(), c.getBlue(), 0.9));
            gc.setLineWidth(1.5);
            gc.strokeRect(key.col() * cellSize + 0.75, key.row() * cellSize + 0.75, cellSize - 1.5, cellSize - 1.5);
        }
    }

    public Pane getNode() {
        return container;
    }
}
