module com.example.openworld {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;

    opens com.example.openworld to javafx.fxml;
    exports com.example.openworld;
}
