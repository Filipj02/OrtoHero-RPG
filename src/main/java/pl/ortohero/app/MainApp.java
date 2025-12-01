package pl.ortohero.app;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class MainApp extends Application {

    public static final int WINDOW_WIDTH = 1280;
    public static final int WINDOW_HEIGHT = 720;

    private int currentMapX = 0; // 0 lub 1
    private int currentMapY = 1; // 0 lub 1; start jak na rysunku (dolny-lewy)


    private boolean up, down, left, right;





    private int getCurrentMapNumber() {
        if (currentMapX == 0 && currentMapY == 0) return 1;
        if (currentMapX == 1 && currentMapY == 0) return 2;
        if (currentMapX == 0 && currentMapY == 1) return 3;
        if (currentMapX == 1 && currentMapY == 1) return 4;
        return -1; // na przyszłość, gdy będzie więcej plansz
    }



    @Override
    public void start(Stage stage) {
        Player player = new Player();
        player.setPosition(50, WINDOW_HEIGHT - 150);


        int cols = WINDOW_WIDTH / TileMap.TILE_SIZE;
        int rows = WINDOW_HEIGHT / TileMap.TILE_SIZE;
        TileMap tileMap = new TileMap(cols, rows);

        Canvas canvas = new Canvas(WINDOW_WIDTH, WINDOW_HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        Pane root = new Pane(canvas);
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.W) up = true;
            if (e.getCode() == KeyCode.S) down = true;
            if (e.getCode() == KeyCode.A) left = true;
            if (e.getCode() == KeyCode.D) right = true;
        });

        scene.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.W) up = false;
            if (e.getCode() == KeyCode.S) down = false;
            if (e.getCode() == KeyCode.A) left = false;
            if (e.getCode() == KeyCode.D) right = false;
        });

        AnimationTimer gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double oldX = player.getX();
                double oldY = player.getY();
                player.update(up, down, left, right);

                if (tileMap.isBlocked(player.getHitboxX(), player.getHitboxY(), player.getHitboxSize())) {
                    player.setPosition(oldX, oldY);
                }


                // przejścia poziome
                if (player.getX() > WINDOW_WIDTH) {           // wyszedł w prawo
                    if (currentMapX < 1) {                    // jest mapa po prawej
                        currentMapX++;
                        player.setPosition(0, player.getY());
                    } else {
                        player.setPosition(WINDOW_WIDTH - 40, player.getY()); // ściana
                    }
                }
                if (player.getX() < -40) {                    // wyszedł w lewo
                    if (currentMapX > 0) {
                        currentMapX--;
                        player.setPosition(WINDOW_WIDTH - 40, player.getY());
                    } else {
                        player.setPosition(0, player.getY());
                    }
                }

// przejścia pionowe
                if (player.getY() > WINDOW_HEIGHT) {          // dół
                    if (currentMapY < 1) {
                        currentMapY++;
                        player.setPosition(player.getX(), 0);
                    } else {
                        player.setPosition(player.getX(), WINDOW_HEIGHT - 40);
                    }
                }
                if (player.getY() < -40) {                    // góra
                    if (currentMapY > 0) {
                        currentMapY--;
                        player.setPosition(player.getX(), WINDOW_HEIGHT - 40);
                    } else {
                        player.setPosition(player.getX(), 0);
                    }
                }



                gc.clearRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
                tileMap.render(gc);
                player.render(gc);

                gc.setFill(javafx.scene.paint.Color.BLACK);
                gc.setFont(javafx.scene.text.Font.font(24));
                String text = "Mapa: " + getCurrentMapNumber();
                gc.fillText(text, WINDOW_WIDTH - 150, 30); // stała pozycja z marginesem
            }
        };
        gameLoop.start();

        stage.setTitle("OrtoHero");
        stage.setScene(scene);
        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
