module com.lertos.spriteeditor {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;


    opens com.lertos.spriteeditor to javafx.fxml;
    exports com.lertos.spriteeditor;
}