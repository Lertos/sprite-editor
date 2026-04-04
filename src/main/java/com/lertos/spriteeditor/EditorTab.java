package com.lertos.spriteeditor;

import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileInputStream;
import java.util.Optional;

public class EditorTab {

    private static final String INITIAL_DIR_FILE_PATH = "C:\\Users\\Dylan\\Downloads";
    private final AppModel model;
    private final BorderPane root;
    private final SpritesheetCanvas canvas;
    private final VBox categoryPanel;

    public EditorTab(AppModel model) {
        this.model = model;
        this.root = new BorderPane();
        this.canvas = new SpritesheetCanvas(model);
        this.categoryPanel = new VBox(8);

        buildUI();

        // TODO: Removes the step of having to create a new category every time. Remove after testing
        AppModel.Category testCat = new AppModel.Category("test", Color.web("#b31010"));

        model.getCategories().add(testCat);
        model.setSelectedCategory(testCat);
    }

    private void buildUI() {
        VBox topBar = buildTopBar();
        root.setTop(topBar);

        ScrollPane canvasScroll = new ScrollPane(canvas.getNode());
        canvasScroll.setFitToWidth(false);
        canvasScroll.setFitToHeight(false);
        canvasScroll.setStyle("-fx-background-color: #2b2b2b;");
        canvasScroll.setPannable(true);
        root.setCenter(canvasScroll);

        VBox rightPanel = buildRightPanel();
        root.setRight(rightPanel);

        model.spritesheetProperty().addListener((obs, old, img) -> canvas.redraw());
        model.cellSizeProperty().addListener((obs, old, val) -> canvas.redraw());
    }

    private VBox buildTopBar() {
        VBox bar = new VBox(8);
        bar.setPadding(new Insets(10, 12, 8, 12));
        bar.setStyle("-fx-background-color: #3c3c3c; -fx-border-color: #555 transparent transparent transparent; -fx-border-width: 0 0 1 0;");

        HBox row1 = new HBox(12);
        row1.setAlignment(Pos.CENTER_LEFT);

        Button loadBtn = new Button("Load Sprite Sheet");
        loadBtn.setStyle("-fx-background-color: #5a7fc4; -fx-text-fill: white; -fx-font-weight: bold;");
        loadBtn.setOnAction(e -> loadSpritesheet());

        Label fileLabel = new Label("No file loaded");
        fileLabel.setStyle("-fx-text-fill: #aaaaaa;");

        model.spritesheetProperty().addListener((obs, old, img) -> {
            if (img != null) {
                fileLabel.setText(String.format("%.0f x %.0f px", img.getWidth(), img.getHeight()));
            } else {
                fileLabel.setText("No file loaded");
            }
        });

        row1.getChildren().addAll(loadBtn, fileLabel);

        HBox row2 = new HBox(12);
        row2.setAlignment(Pos.CENTER_LEFT);

        Label cellSizeLabel = new Label("Cell Size (px):");
        cellSizeLabel.setStyle("-fx-text-fill: #cccccc;");

        Spinner<Integer> cellSizeSpinner = new Spinner<>(1, 2048, model.getCellSize(), 1);
        cellSizeSpinner.setEditable(true);
        cellSizeSpinner.setPrefWidth(80);
        cellSizeSpinner.valueProperty().addListener((obs, old, val) -> {
            model.setCellSize(val);
            model.clearAllAssignments();
            canvas.redraw();
        });

        Label gridInfoLabel = new Label();
        gridInfoLabel.setStyle("-fx-text-fill: #aaaaaa;");
        gridInfoLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            if (model.getSpritesheet() != null) {
                return model.getColumns() + " cols × " + model.getRows() + " rows";
            }
            return "";
        }, model.spritesheetProperty(), model.cellSizeProperty()));

        Button clearBtn = new Button("Clear All Tags");
        clearBtn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white;");
        clearBtn.setOnAction(e -> {
            model.clearAllAssignments();
            canvas.redraw();
        });

        row2.getChildren().addAll(cellSizeLabel, cellSizeSpinner, gridInfoLabel, clearBtn);

        bar.getChildren().addAll(row1, row2);
        return bar;
    }

    private VBox buildRightPanel() {
        VBox panel = new VBox(12);
        panel.setPrefWidth(220);
        panel.setPadding(new Insets(12));
        panel.setStyle("-fx-background-color: #3c3c3c; -fx-border-color: transparent transparent transparent #555; -fx-border-width: 0 0 0 1;");

        Label title = new Label("Categories");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #eeeeee;");

        Button addCatBtn = new Button("Add Category");
        addCatBtn.setMaxWidth(Double.MAX_VALUE);
        addCatBtn.setStyle("-fx-background-color: #4a7a4a; -fx-text-fill: white; -fx-font-weight: bold;");
        addCatBtn.setOnAction(e -> showAddCategoryDialog());

        categoryPanel.setFillWidth(true);

        model.getCategories().addListener((ListChangeListener<AppModel.Category>) c -> rebuildCategoryList());

        ScrollPane catScroll = new ScrollPane(categoryPanel);
        catScroll.setFitToWidth(true);
        catScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(catScroll, Priority.ALWAYS);

        panel.getChildren().addAll(title, addCatBtn, catScroll);
        return panel;
    }

    private void rebuildCategoryList() {
        categoryPanel.getChildren().clear();
        for (AppModel.Category cat : model.getCategories()) {
            categoryPanel.getChildren().add(buildCategoryRow(cat));
        }
    }

    private Node buildCategoryRow(AppModel.Category cat) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(6, 8, 6, 8));
        row.setStyle("-fx-background-radius: 6;");
        row.setMaxWidth(Double.MAX_VALUE);

        Runnable updateStyle = () -> {
            boolean selected = cat.equals(model.getSelectedCategory());
            if (selected) {
                row.setStyle("-fx-background-color: #4a5568; -fx-background-radius: 6; -fx-cursor: hand;");
            } else {
                row.setStyle("-fx-background-color: #2d2d2d; -fx-background-radius: 6; -fx-cursor: hand;");
            }
        };

        model.selectedCategoryProperty().addListener((obs, old, newCat) -> updateStyle.run());
        updateStyle.run();

        javafx.scene.shape.Rectangle colorSwatch = new javafx.scene.shape.Rectangle(18, 18);
        colorSwatch.setArcWidth(4);
        colorSwatch.setArcHeight(4);
        colorSwatch.setFill(cat.getColor());
        colorSwatch.setStroke(Color.gray(0.6));

        Label nameLabel = new Label(cat.getName());
        nameLabel.setStyle("-fx-text-fill: #eeeeee;");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);
        nameLabel.setMaxWidth(Double.MAX_VALUE);

        Button deleteBtn = new Button("✕");
        deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #cc4444; -fx-font-size: 11px; -fx-padding: 2 4;");
        deleteBtn.setOnAction(e -> {
            model.getCategories().remove(cat);
            model.getCellAssignments().values().removeIf(c -> c.equals(cat));
            if (cat.equals(model.getSelectedCategory())) {
                model.setSelectedCategory(null);
            }
            canvas.redraw();
        });

        row.getChildren().addAll(colorSwatch, nameLabel, deleteBtn);

        row.setOnMouseClicked(e -> {
            if (cat.equals(model.getSelectedCategory())) {
                model.setSelectedCategory(null);
            } else {
                model.setSelectedCategory(cat);
            }
        });

        return row;
    }

    private void showAddCategoryDialog() {
        Dialog<AppModel.Category> dialog = new Dialog<>();
        dialog.setTitle("Add Category");
        dialog.setHeaderText("Create a new category");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16, 16, 8, 16));

        TextField nameField = new TextField();
        nameField.setPromptText("Category name");

        ColorPicker colorPicker = new ColorPicker(generateNextColor());

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Color:"), 0, 1);
        grid.add(colorPicker, 1, 1);

        dialog.getDialogPane().setContent(grid);

        Node addButton = dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);
        nameField.textProperty().addListener((obs, old, val) -> addButton.setDisable(val.trim().isEmpty()));

        dialog.setResultConverter(btn -> {
            if (btn == addButtonType) {
                return new AppModel.Category(nameField.getText().trim(), colorPicker.getValue());
            }
            return null;
        });

        Optional<AppModel.Category> result = dialog.showAndWait();
        result.ifPresent(cat -> {
            model.getCategories().add(cat);
            model.setSelectedCategory(cat);
        });
    }

    private Color generateNextColor() {
        return Color.color(Math.random(), Math.random(), Math.random());
    }

    private void loadSpritesheet() {
        FileChooser fc = new FileChooser();
        //TODO: Need to either save this in some config/user data file so after they choose it once it will continue to use it
        File initialDir = new File(INITIAL_DIR_FILE_PATH);

        if (initialDir.exists() && initialDir.isDirectory()) {
            fc.setInitialDirectory(initialDir);
        }

        fc.setTitle("Open Sprite Sheet");

        fc.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File file = fc.showOpenDialog(root.getScene().getWindow());
        if (file != null) {
            try {
                Image image = new Image(new FileInputStream(file));
                model.setSpritesheet(image);
                model.clearAllAssignments();
                canvas.redraw();
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to load image: " + ex.getMessage());
                alert.showAndWait();
            }
        }
    }

    public Node getRoot() {
        return root;
    }
}
