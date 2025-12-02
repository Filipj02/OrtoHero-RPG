package pl.ortohero.app;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class MainApp extends Application {

    public static final int WINDOW_WIDTH = 1280;
    public static final int WINDOW_HEIGHT = 720;

    // --- STANY GRY ---
    // WALKING: Gracz chodzi
    // DIALOGUE: Wyświetla się okno zadania (blokada ruchu)
    private enum GameState { WALKING, DIALOGUE }
    private GameState gameState = GameState.WALKING;

    private int currentMapX = 0;
    private int currentMapY = 0;

    private TileMap tileMap;
    private List<GameObject> currentObjects = new ArrayList<>(); // Lista obiektów na obecnej mapie

    private boolean up, down, left, right, spacePressed; // Doszła spacja
    private boolean spaceWasProcessed = false; // Żeby jedno wciśnięcie nie klikało 100 razy na sekundę

    // Obiekt z którym aktualnie gadamy
    private GameObject activeObject = null;

    private int getCurrentMapNumber() {
        if (currentMapX == 0 && currentMapY == 0) return 1;
        if (currentMapX == 1 && currentMapY == 0) return 2;
        if (currentMapX == 0 && currentMapY == 1) return 3;
        if (currentMapX == 1 && currentMapY == 1) return 4;
        return -1;
    }

    @Override
    public void start(Stage stage) {
        Player player = new Player();
        player.setPosition(50, WINDOW_HEIGHT - 150);

        int cols = WINDOW_WIDTH / TileMap.TILE_SIZE;
        int rows = WINDOW_HEIGHT / TileMap.TILE_SIZE;

        tileMap = new TileMap(cols, rows);

        // Ładujemy mapę i obiekty na start
        refreshMapData(cols, rows);

        Canvas canvas = new Canvas(WINDOW_WIDTH, WINDOW_HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Pane root = new Pane(canvas);
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.W) up = true;
            if (e.getCode() == KeyCode.S) down = true;
            if (e.getCode() == KeyCode.A) left = true;
            if (e.getCode() == KeyCode.D) right = true;
            if (e.getCode() == KeyCode.SPACE) spacePressed = true;
        });

        scene.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.W) up = false;
            if (e.getCode() == KeyCode.S) down = false;
            if (e.getCode() == KeyCode.A) left = false;
            if (e.getCode() == KeyCode.D) right = false;
            if (e.getCode() == KeyCode.SPACE) {
                spacePressed = false;
                spaceWasProcessed = false; // Reset blokady spacji po puszczeniu klawisza
            }
        });

        AnimationTimer gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateGame(player);
                renderGame(gc, player);
            }
        };
        gameLoop.start();

        stage.setTitle("OrtoHero RPG");
        stage.setScene(scene);
        stage.show();
    }

    private void updateGame(Player player) {
        // 1. LOGIKA W TRAKCIE CHODZENIA
        if (gameState == GameState.WALKING) {
            double oldX = player.getX();
            double oldY = player.getY();

            player.update(up, down, left, right);

            // Kolizja z blokami
            if (tileMap.isBlocked(player.getHitboxX(), player.getHitboxY(), player.getHitboxSize())) {
                player.setPosition(oldX, oldY);
            }

            // Obsługa zmiany map (kod z poprzedniego etapu)
            handleMapSwitching(player);

            // INTERAKCJA POD SPACJĄ
            if (spacePressed && !spaceWasProcessed) {
                // Sprawdzamy czy stoimy obok jakiegoś obiektu
                for (GameObject obj : currentObjects) {
                    if (obj.isPlayerClose(player)) {
                        System.out.println("Interakcja z: " + obj.getName());
                        activeObject = obj;
                        gameState = GameState.DIALOGUE; // Zmieniamy stan na dialog -> blokada ruchu
                        spaceWasProcessed = true;

                        // Zresetuj ruch gracza, żeby nie "leciał" w tle
                        up = false; down = false; left = false; right = false;
                        break;
                    }
                }
            }
        }
        // 2. LOGIKA W TRAKCIE ZADANIA / DIALOGU
        else if (gameState == GameState.DIALOGUE) {
            // Tutaj gracz się NIE rusza. Czeka na zamknięcie okna.
            // Na razie symulujemy wyjście SPACJĄ
            if (spacePressed && !spaceWasProcessed) {
                System.out.println("Koniec interakcji.");
                gameState = GameState.WALKING; // Wracamy do gry
                activeObject = null;
                spaceWasProcessed = true;
            }
        }
    }

    private void handleMapSwitching(Player player) {
        // (Ten kod jest taki sam jak wcześniej, tylko wywołuje refreshMapData)
        // W PRAWO
        if (player.getX() > WINDOW_WIDTH - 20) {
            if (currentMapX < 1) {
                currentMapX++;
                player.setPosition(20, player.getY());
                refreshMapData(WINDOW_WIDTH/32, WINDOW_HEIGHT/32);
            } else {
                player.setPosition(WINDOW_WIDTH - 40, player.getY());
            }
        }
        // W LEWO
        if (player.getX() < -10) {
            if (currentMapX > 0) {
                currentMapX--;
                player.setPosition(WINDOW_WIDTH - 50, player.getY());
                refreshMapData(WINDOW_WIDTH/32, WINDOW_HEIGHT/32);
            } else {
                player.setPosition(0, player.getY());
            }
        }
        // W DÓŁ i W GÓRĘ (skróciłem dla czytelności, wklej tu wersję z poprzedniego kroku jeśli potrzebujesz)
        // ... (Logika Y bez zmian)
    }

    private void refreshMapData(int cols, int rows) {
        int mapNum = getCurrentMapNumber();

        // 1. Ładujemy kafelki
        if (mapNum == 1) tileMap.loadMapData(MapDefinitions.getMap1(cols, rows));
        else if (mapNum == 2) tileMap.loadMapData(MapDefinitions.getMap2(cols, rows));
        else tileMap.loadMapData(MapDefinitions.getMap1(cols, rows));

        // 2. Ładujemy obiekty (To jest nowe!)
        currentObjects = MapDefinitions.getObjects(mapNum);
    }

    private void renderGame(GraphicsContext gc, Player player) {
        // Czyść ekran
        gc.clearRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);

        // Rysuj mapę
        tileMap.render(gc);

        // Rysuj obiekty
        for (GameObject obj : currentObjects) {
            obj.render(gc);
        }

        // Rysuj gracza
        player.render(gc);

        // Rysuj HUD (interfejs)
        if (gameState == GameState.DIALOGUE && activeObject != null) {
            // Półprzezroczyste tło
            gc.setFill(Color.rgb(0, 0, 0, 0.7));
            gc.fillRect(100, 100, WINDOW_WIDTH - 200, WINDOW_HEIGHT - 200);

            // Tekst zadania
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font(30));
            gc.fillText("Zadanie: " + activeObject.getName(), 150, 150);
            gc.setFont(Font.font(20));
            gc.fillText("Naciśnij SPACJĘ, aby zamknąć (Test)", 150, 200);
        }

        // Info debugowe
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font(14));
        gc.fillText("Mapa: " + getCurrentMapNumber() + " | Stan: " + gameState, 10, 20);
    }

    public static void main(String[] args) {
        launch(args);
    }
}