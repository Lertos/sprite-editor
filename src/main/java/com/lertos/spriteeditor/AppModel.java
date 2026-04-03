package com.lertos.spriteeditor;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

public class AppModel {

    private static final int DEFAULT_CELL_SIZE = 16;

    private final ObjectProperty<Image> spritesheet = new SimpleObjectProperty<>();
    private final IntegerProperty cellSize = new SimpleIntegerProperty(DEFAULT_CELL_SIZE);
    private final ObservableList<Category> categories = FXCollections.observableArrayList();
    private final ObjectProperty<Category> selectedCategory = new SimpleObjectProperty<>();

    private final Map<CellKey, Category> cellAssignments = new HashMap<>();

    public ObjectProperty<Image> spritesheetProperty() {
        return spritesheet;
    }

    public Image getSpritesheet() {
        return spritesheet.get();
    }

    public void setSpritesheet(Image image) {
        spritesheet.set(image);
    }

    public IntegerProperty cellSizeProperty() {
        return cellSize;
    }

    public int getCellSize() {
        return cellSize.get();
    }

    public void setCellSize(int size) {
        cellSize.set(size);
    }

    public ObservableList<Category> getCategories() {
        return categories;
    }

    public ObjectProperty<Category> selectedCategoryProperty() {
        return selectedCategory;
    }

    public Category getSelectedCategory() {
        return selectedCategory.get();
    }

    public void setSelectedCategory(Category category) {
        selectedCategory.set(category);
    }

    public Map<CellKey, Category> getCellAssignments() {
        return cellAssignments;
    }

    public void assignCell(int col, int row, Category category) {
        CellKey key = new CellKey(col, row);
        if (category == null) {
            cellAssignments.remove(key);
        } else {
            cellAssignments.put(key, category);
        }
    }

    public void clearAllAssignments() {
        cellAssignments.clear();
    }

    public int getColumns() {
        if (spritesheet.get() == null || cellSize.get() <= 0) return 0;
        return (int) (spritesheet.get().getWidth() / cellSize.get());
    }

    public int getRows() {
        if (spritesheet.get() == null || cellSize.get() <= 0) return 0;
        return (int) (spritesheet.get().getHeight() / cellSize.get());
    }

    public record CellKey(int col, int row) {}

    public static class Category {
        private final String name;
        private final Color color;

        public Category(String name, Color color) {
            this.name = name;
            this.color = color;
        }

        public String getName() {
            return name;
        }

        public Color getColor() {
            return color;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
