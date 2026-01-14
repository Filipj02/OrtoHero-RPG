package pl.ortohero.app;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class MainApp extends Application {

    public static final int WINDOW_WIDTH = 1280;
    public static final int WINDOW_HEIGHT = 736;

    private GameEngine engine;
    private VBox menuBox;

    @Override
    public void start(Stage stage) {
        // 1. BUDOWANIE UI (MENU)
        createMenuUI(); // Musimy to zrobić najpierw, żeby przekazać menu do silnika

        // 2. INICJALIZACJA SILNIKA GRY
        engine = new GameEngine(menuBox);

        // 3. ŁĄCZENIE SUWAKA GŁOŚNOŚCI (Jeśli chcesz)
        Slider volumeSlider = (Slider) menuBox.getChildren().get(1); // Brzydki hack, ale działa :D
        engine.getSoundManager().bindVolumeSlider(volumeSlider);

        // 4. KONFIGURACJA SCENY
        Canvas canvas = new Canvas(WINDOW_WIDTH, WINDOW_HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        StackPane root = new StackPane(canvas, menuBox);
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        // 5. PRZEKAZYWANIE STEROWANIA DO SILNIKA
        root.setOnMouseClicked(e -> {
            root.requestFocus();
            engine.handleMouseClick(e.getX(), e.getY());
        });

        scene.setOnKeyPressed(e -> engine.handleKeyPressed(e));
        scene.setOnKeyReleased(e -> engine.handleKeyReleased(e));
        scene.setOnKeyTyped(e -> engine.handleKeyTyped(e.getCharacter()));

        // 6. PĘTLA GRY
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                engine.update();
                engine.render(gc);
            }
        }.start();

        stage.setTitle("OrtoHero RPG - Final Clean Architecture");
        stage.setScene(scene);
        stage.show();
        root.requestFocus();
    }

    private void createMenuUI() {
        Label lbl = new Label("Głośność");
        lbl.setTextFill(Color.WHITE);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        Slider vol = new Slider(0, 1, 0.5);
        vol.setMaxWidth(200);

        Button saveBtn = new Button("Zapisz i Wyjdź");
        saveBtn.setStyle("-fx-font-size: 16px; -fx-padding: 10px 20px; -fx-background-color: gold;");
        saveBtn.setOnAction(e -> engine.saveAndExit());

        menuBox = new VBox(20, lbl, vol, saveBtn);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setStyle("-fx-background-color: rgba(0,0,0,0.8); -fx-padding: 50; -fx-background-radius: 30;");
        menuBox.setMaxSize(400, 300);
        menuBox.setVisible(false); // Domyślnie ukryte
    }

    public static void main(String[] args) {
        launch(args);
    }
}