module pl.ortohero.app.ortohero {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;


    opens pl.ortohero.app to javafx.fxml, com.google.gson;
    exports pl.ortohero.app;
}