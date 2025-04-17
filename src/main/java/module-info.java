module com.example.openworld {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.openworld to javafx.fxml;
    exports com.example.openworld;
}