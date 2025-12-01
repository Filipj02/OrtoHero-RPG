module pl.ortohero.app.ortohero {
    requires javafx.controls;
    requires javafx.fxml;


    opens pl.ortohero.app to javafx.fxml;
    exports pl.ortohero.app;
}