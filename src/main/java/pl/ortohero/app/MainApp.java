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
    private enum GameState { WALKING, DIALOGUE }
    private GameState gameState = GameState.WALKING;

    // --- MAPA I POZYCJA ---
    private int currentMapX = 0;
    private int currentMapY = 0; // Start na mapie (0,0) -> Mapa 1

    // --- OBIEKTY GŁÓWNE ---
    private TileMap tileMap;
    private List<GameObject> currentObjects = new ArrayList<>();
    private WordBank wordBank;
    private TaskView taskView;

    // --- STEROWANIE ---
    private boolean up, down, left, right, spacePressed;
    private boolean spaceWasProcessed = false;

    // --- LOGIKA ZADANIA ---
    private GameObject activeObject = null;
    private Word currentWord;       // Aktualne słowo do rozwiązania
    private String message = "";    // Komunikat (Brawo/Błąd)
    private String typedAnswer = ""; // To co wpisał gracz (np. "rz")

    private int getCurrentMapNumber() {
        if (currentMapX == 0 && currentMapY == 0) return 1;
        if (currentMapX == 1 && currentMapY == 0) return 2;
        if (currentMapX == 0 && currentMapY == 1) return 3;
        if (currentMapX == 1 && currentMapY == 1) return 4;
        return -1;
    }

    @Override
    public void start(Stage stage) {
        // 1. Inicjalizacja Logiki
        Player player = new Player();
        player.setPosition(50, WINDOW_HEIGHT - 150);

        wordBank = new WordBank();
        taskView = new TaskView();

        // 2. Inicjalizacja Mapy
        int cols = WINDOW_WIDTH / TileMap.TILE_SIZE;
        int rows = WINDOW_HEIGHT / TileMap.TILE_SIZE;
        tileMap = new TileMap(cols, rows);
        refreshMapData(cols, rows); // Załaduj pierwszą mapę

        // 3. Konfiguracja JavaFX
        Canvas canvas = new Canvas(WINDOW_WIDTH, WINDOW_HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Pane root = new Pane(canvas);
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        // --- NAPRAWA FOCUSU (WAŻNE DLA WPISYWANIA LITER) ---
        root.setFocusTraversable(true);
        root.setOnMouseClicked(e -> root.requestFocus()); // Kliknięcie przywraca sterowanie
        root.requestFocus(); // Wymuś skupienie na starcie

        // 4. OBSŁUGA KLAWIATURY - RUCH (STRZAŁKI)
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.UP) up = true;
            if (e.getCode() == KeyCode.DOWN) down = true;
            if (e.getCode() == KeyCode.LEFT) left = true;
            if (e.getCode() == KeyCode.RIGHT) right = true;
            if (e.getCode() == KeyCode.SPACE) spacePressed = true;
        });

        scene.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.UP) up = false;
            if (e.getCode() == KeyCode.DOWN) down = false;
            if (e.getCode() == KeyCode.LEFT) left = false;
            if (e.getCode() == KeyCode.RIGHT) right = false;
            if (e.getCode() == KeyCode.SPACE) {
                spacePressed = false;
                spaceWasProcessed = false;
            }
        });

        // 5. OBSŁUGA KLAWIATURY - PISANIE (LITERY)
        scene.setOnKeyTyped(e -> {
            // Działa tylko w trybie dialogu
            if (gameState == GameState.DIALOGUE && currentWord != null) {
                String character = e.getCharacter();

                // Ignoruj znaki sterujące (enter, tab, puste)
                if (character.trim().isEmpty() || character.equals("\r") || character.equals("\n")) {
                    return;
                }

                // Dodaj literę do bufora
                typedAnswer += character;
                System.out.println("DEBUG: Wpisano: " + typedAnswer);

                // Sprawdź odpowiedź
                if (currentWord.getTarget().startsWith(typedAnswer)) {
                    // Pasuje do początku, sprawdzamy czy to całość
                    if (currentWord.checkAnswer(typedAnswer)) {
                        message = "BRAWO! Prawidłowa odpowiedź.";
                        // Tu kiedyś dodasz: player.addXP(10);
                    } else {
                        // Pasuje, ale to jeszcze nie koniec (np. wpisano "r", a ma być "rz")
                        message = "Wpisano: " + typedAnswer + "...";
                    }
                } else {
                    // Błąd
                    message = "BŁĄD! Reguła: " + currentWord.getRule();
                    typedAnswer = ""; // Resetuj wpisywanie
                }
            }
        });

        // 6. GŁÓWNA PĘTLA GRY
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
        // --- STAN: CHODZENIE ---
        if (gameState == GameState.WALKING) {
            double oldX = player.getX();
            double oldY = player.getY();

            player.update(up, down, left, right);

            // Kolizje z blokami
            if (tileMap.isBlocked(player.getHitboxX(), player.getHitboxY(), player.getHitboxSize())) {
                player.setPosition(oldX, oldY);
            }

            handleMapSwitching(player);

            // Interakcja (SPACJA) - wejście w zadanie
            if (spacePressed && !spaceWasProcessed) {
                for (GameObject obj : currentObjects) {
                    if (obj.isPlayerClose(player)) {
                        activeObject = obj;
                        gameState = GameState.DIALOGUE; // ZATRZYMUJEMY GRĘ
                        spaceWasProcessed = true;

                        // Losowanie zadania
                        List<Word> words = wordBank.getRandomWords(1, 1);
                        if (!words.isEmpty()) {
                            currentWord = words.get(0);
                            message = "";       // Czyść stare wiadomości
                            typedAnswer = "";   // Czyść stary wpisany tekst
                        } else {
                            currentWord = null;
                            message = "Brak zadań w bazie!";
                        }

                        // Reset ruchu (żeby postać nie szła w miejscu)
                        up = false; down = false; left = false; right = false;
                        break;
                    }
                }
            }
        }
        // --- STAN: DIALOG / ZADANIE ---
        else if (gameState == GameState.DIALOGUE) {
            // Wyjście z zadania (SPACJA)
            if (spacePressed && !spaceWasProcessed) {
                gameState = GameState.WALKING; // WRACAMY DO GRY
                activeObject = null;
                currentWord = null;
                spaceWasProcessed = true;
            }
        }
    }

    private void renderGame(GraphicsContext gc, Player player) {
        // 1. Czyść ekran
        gc.clearRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);

        // 2. Rysuj świat
        tileMap.render(gc);
        for (GameObject obj : currentObjects) {
            obj.render(gc);
        }
        player.render(gc);

        // 3. Rysuj nakładkę zadania (tylko w trybie DIALOGUE)
        if (gameState == GameState.DIALOGUE) {
            taskView.render(gc, WINDOW_WIDTH, WINDOW_HEIGHT, currentWord, message);
        }

        // Debug info (opcjonalne)
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font(12));
        gc.fillText("Mapa: " + getCurrentMapNumber() + " | Stan: " + gameState, 10, 20);
    }

    private void handleMapSwitching(Player player) {
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
        // W DÓŁ
        if (player.getY() > WINDOW_HEIGHT - 20) {
            if (currentMapY < 1) {
                currentMapY++;
                player.setPosition(player.getX(), 20);
                refreshMapData(WINDOW_WIDTH/32, WINDOW_HEIGHT/32);
            } else {
                player.setPosition(player.getX(), WINDOW_HEIGHT - 40);
            }
        }
        // W GÓRĘ
        if (player.getY() < -10) {
            if (currentMapY > 0) {
                currentMapY--;
                player.setPosition(player.getX(), WINDOW_HEIGHT - 50);
                refreshMapData(WINDOW_WIDTH/32, WINDOW_HEIGHT/32);
            } else {
                player.setPosition(player.getX(), 0);
            }
        }
    }

    private void refreshMapData(int cols, int rows) {
        int mapNum = getCurrentMapNumber();

        // Ładujemy kafelki
        if (mapNum == 1) tileMap.loadMapData(MapDefinitions.getMap1(cols, rows));
        else if (mapNum == 2) tileMap.loadMapData(MapDefinitions.getMap2(cols, rows));
        else tileMap.loadMapData(MapDefinitions.getMap1(cols, rows)); // Domyślna

        // Ładujemy obiekty
        currentObjects = MapDefinitions.getObjects(mapNum);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
