package pl.ortohero.app;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainApp extends Application {

    public static final int WINDOW_WIDTH = 1280;
    public static final int WINDOW_HEIGHT = 736;

    private enum GameState { WALKING, DIALOGUE, INVENTORY, WIN }
    private GameState gameState = GameState.WALKING;

    private int currentMapX = 0;
    private int currentMapY = 0;

    private TileMap tileMap;
    private List<GameObject> currentObjects = new ArrayList<>();
    private WordBank wordBank;
    private TaskView taskView;
    private InventoryView inventoryView;

    private boolean up, down, left, right, spacePressed;
    private boolean spaceWasProcessed = false;
    private boolean eKeyWasProcessed = false;

    private GameObject activeObject = null;
    private Word currentWord;
    private String message = "";

    private int remainingWordsToSolve = 0;
    private boolean isTaskSolved = false;

    private Player player;
    private Image heartImg;
    // USUNIĘTO: private Image heroImg; -> Teraz Player sam zarządza swoimi grafikami
    private String typedAnswer = ""; // To będzie pamiętać, że wpisałeś już "r" i czeka na "z"


    @Override
    public void start(Stage stage) {
        player = new Player(); // Player sam załaduje swoje grafiki w konstruktorze
        player.setPosition(50, WINDOW_HEIGHT - 150);

        wordBank = new WordBank();
        taskView = new TaskView();
        inventoryView = new InventoryView();

        int cols = WINDOW_WIDTH / TileMap.TILE_SIZE;
        int rows = WINDOW_HEIGHT / TileMap.TILE_SIZE;
        tileMap = new TileMap(cols, rows);
        refreshMapData(cols, rows);

        Canvas canvas = new Canvas(WINDOW_WIDTH, WINDOW_HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Pane root = new Pane(canvas);
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        root.setFocusTraversable(true);
        root.setOnMouseClicked(e -> root.requestFocus());
        root.requestFocus();

        try {
            heartImg = new Image(getClass().getResourceAsStream("/images/heart.png"));
            // USUNIĘTO stare ładowanie heroImg
        } catch (Exception e) {
            System.err.println("Błąd ładowania grafik UI: " + e.getMessage());
        }

        // --- OBSŁUGA KLAWIATURY ---
        scene.setOnKeyPressed(e -> {
            if (gameState == GameState.WALKING) {
                if (e.getCode() == KeyCode.UP || e.getCode() == KeyCode.W) up = true;
                if (e.getCode() == KeyCode.DOWN || e.getCode() == KeyCode.S) down = true;
                if (e.getCode() == KeyCode.LEFT || e.getCode() == KeyCode.A) left = true;
                if (e.getCode() == KeyCode.RIGHT || e.getCode() == KeyCode.D) right = true;
                if (e.getCode() == KeyCode.SPACE) spacePressed = true;
                if (e.getCode() == KeyCode.E && !eKeyWasProcessed) {
                    toggleInventory();
                    eKeyWasProcessed = true;
                }
            }
            else if (gameState == GameState.INVENTORY) {
                if (e.getCode() == KeyCode.E || e.getCode() == KeyCode.ESCAPE) {
                    gameState = GameState.WALKING;
                }
            }
            else if (gameState == GameState.DIALOGUE) {
                if (e.getCode() == KeyCode.ESCAPE) {
                    gameState = GameState.WALKING;
                    activeObject = null;
                }
                if (isTaskSolved && e.getCode() == KeyCode.SPACE) {
                    spacePressed = true;
                }
            }
            else if (gameState == GameState.WIN) {
                if (e.getCode() == KeyCode.SPACE) spacePressed = true;
            }
        });

        scene.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.UP || e.getCode() == KeyCode.W) up = false;
            if (e.getCode() == KeyCode.DOWN || e.getCode() == KeyCode.S) down = false;
            if (e.getCode() == KeyCode.LEFT || e.getCode() == KeyCode.A) left = false;
            if (e.getCode() == KeyCode.RIGHT || e.getCode() == KeyCode.D) right = false;
            if (e.getCode() == KeyCode.SPACE) {
                spacePressed = false;
                spaceWasProcessed = false;
            }
            if (e.getCode() == KeyCode.E) eKeyWasProcessed = false;
        });

        // --- LOGIKA PISANIA ---
        // --- LOGIKA PISANIA (NAPRAWIONA OBSŁUGA DWUZNAKÓW) ---
        // --- LOGIKA PISANIA (INTELIGENTNE BŁĘDY) ---
        scene.setOnKeyTyped(e -> {
            if (isTaskSolved) return;

            if (gameState == GameState.DIALOGUE && currentWord != null) {
                String charInput = e.getCharacter();
                // Ignoruj puste znaki
                if (charInput.trim().isEmpty()) return;

                // Dodajemy literę do tego co wpisał gracz
                typedAnswer += charInput;
                String target = currentWord.getTarget();

                // 1. CZY WPISANO DOBRZE? (Pasuje do początku lub całości)
                if (target.toLowerCase().startsWith(typedAnswer.toLowerCase())) {

                    // Czy to już całe słowo?
                    if (typedAnswer.equalsIgnoreCase(target)) {
                        // --- SUKCES ---
                        player.addSuccess();
                        remainingWordsToSolve--;

                        if (remainingWordsToSolve > 0) {
                            loadNextWord();
                            message = "Dobrze! Zostało: " + remainingWordsToSolve;
                        } else {
                            isTaskSolved = true;
                            if (activeObject != null && activeObject.getName().equals("BOSS")) {
                                message = "BOSS POKONANY! GRATULACJE! [SPACJA]";
                            } else {
                                message = "ZADANIE WYKONANE! [SPACJA]";
                            }
                        }
                    } else {
                        // To dopiero początek dobrej odpowiedzi (np. wpisano "r", a ma być "rz")
                        message = "Wpisano: " + typedAnswer + "...";
                    }
                }
                // 2. CZY TO BŁĄD ORTOGRAFICZNY? (Karanie życiem)
                else if (isOrthographicError(target, typedAnswer)) {
                    player.loseLife();
                    typedAnswer = ""; // Resetujemy wpisywanie

                    if (player.getLives() <= 0) {
                        message = "GAME OVER! [SPACJA] restart.";
                        isTaskSolved = true;
                    } else {
                        message = "BŁĄD ORTOGRAFICZNY! Tracisz życie.";
                    }
                }
                // 3. PRZYPADKOWA LITERA (Ignorowanie)
                else {
                    // Cofamy wpisanie tej błędnej litery
                    typedAnswer = typedAnswer.substring(0, typedAnswer.length() - 1);
                    // Opcjonalnie: wyświetl info, że ignorowano
                    // message = "Pudło! Wpisz poprawną literę.";
                }
            }
        });

        scene.setOnMouseClicked(e -> {
            if (gameState == GameState.DIALOGUE && !isTaskSolved) {
                String clickedItem = taskView.handleClick(e.getX(), e.getY(), WINDOW_WIDTH, WINDOW_HEIGHT, player);
                if (clickedItem != null) {
                    useItemInCombat(clickedItem);
                    root.requestFocus();
                }
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

        stage.setTitle("OrtoHero RPG - Final Version");
        stage.setScene(scene);
        stage.show();
    }

    private void toggleInventory() {
        gameState = (gameState == GameState.WALKING) ? GameState.INVENTORY : GameState.WALKING;
    }

    private void updateGame(Player player) {
        if (gameState == GameState.WALKING) {

            // 1. Zapamiętujemy starą pozycję
            double oldX = player.getX();
            double oldY = player.getY();

            // 2. Wykonujemy ruch (i animację) JEDEN RAZ
            player.update(up, down, left, right);

            // Pobieramy nową pozycję, w której chciałby znaleźć się gracz
            double newX = player.getX();
            double newY = player.getY();

            // 3. Sprawdzamy KOLIZJE POZIOME (X)
            // Ustawiamy gracza na nowym X, ale starym Y, żeby sprawdzić tylko bok
            player.setPosition(newX, oldY);
            if (tileMap.isBlocked(player.getHitboxX(), player.getHitboxY(), player.getHitboxSize())
                    || checkObjectCollision(player)) {
                newX = oldX; // Jeśli uderzył, cofamy X
            }

            // 4. Sprawdzamy KOLIZJE PIONOWE (Y)
            // Ustawiamy gracza na (ewentualnie poprawionym) X i nowym Y
            player.setPosition(newX, newY);
            if (tileMap.isBlocked(player.getHitboxX(), player.getHitboxY(), player.getHitboxSize())
                    || checkObjectCollision(player)) {
                newY = oldY; // Jeśli uderzył, cofamy Y
            }

            // 5. Finalnie aktualizujemy pozycję na tę bezpieczną
            player.setPosition(newX, newY);

            handleMapSwitching(player);

            if (spacePressed && !spaceWasProcessed) {
                interactWithObject(player);
                spaceWasProcessed = true;
            }
        }
        else if (gameState == GameState.DIALOGUE) {
            if (spacePressed && !spaceWasProcessed) {
                if (isTaskSolved) {
                    if (player.getLives() <= 0) restartGame();
                    else handleTaskSuccess(player);
                    isTaskSolved = false;
                }
                spaceWasProcessed = true;
            }
        }
        else if (gameState == GameState.WIN) {
            if (spacePressed && !spaceWasProcessed) {
                restartGame();
                spaceWasProcessed = true;
            }
        }
    }

    private void interactWithObject(Player player) {
        for (GameObject obj : currentObjects) {
            if (obj.isPlayerClose(player)) {

                // 1. Sprawdzenie BOSSA
                if (obj.getName().equals("BOSS")) {
                    if (player.getLevel() < 10) {
                        // --- BLOKADA LVL ---
                        gameState = GameState.DIALOGUE;
                        isTaskSolved = true; // Ustawiamy true, żeby SPACJA zamknęła okno

                        // HACK: Musimy ustawić jakiekolwiek currentWord, żeby TaskView narysował tło!
                        // Tworzymy puste słowo, którego nie widać
                        currentWord = new Word("BLOKADA", " ", "Wymagany 10 poziom", 1);

                        message = "ZA NISKI POZIOM! (Wymagany: 10)";
                        return;
                    } else {
                        // Boss Fight!
                        remainingWordsToSolve = 15;
                        startCombat(obj);
                        return;
                    }
                }

                // 2. Zwykłe obiekty
                remainingWordsToSolve = 5;
                startCombat(obj);
                return;
            }
        }
    }

    private void startCombat(GameObject obj) {
        activeObject = obj;
        gameState = GameState.DIALOGUE;
        isTaskSolved = false;
        loadNextWord();
        up = false; down = false; left = false; right = false;
        player.resetAnimationState(); // Zatrzymanie animacji gracza
    }

    // Sprawdza, czy wpisana litera to typowy błąd ortograficzny
    private boolean isOrthographicError(String target, String input) {
        target = target.toLowerCase();
        input = input.toLowerCase();

        // Pobieramy ostatnią wpisaną literę (bo to ona spowodowała błąd)
        String lastChar = input.substring(input.length() - 1);

        // 1. Para RZ - Ż
        if (target.startsWith("rz") && lastChar.equals("ż")) return true;
        if (target.startsWith("ż") && lastChar.equals("r")) return true; // Gracz chciał wpisać rz

        // 2. Para CH - H
        if (target.startsWith("ch") && lastChar.equals("h")) return true;
        if (target.startsWith("h") && lastChar.equals("c")) return true; // Gracz chciał wpisać ch

        // 3. Para U - Ó
        if (target.startsWith("u") && lastChar.equals("ó")) return true;
        if (target.startsWith("ó") && lastChar.equals("u")) return true;

        // 4. Para SZ - S (opcjonalnie, jeśli chcesz)
        if (target.startsWith("sz") && lastChar.equals("s")) return false; // S to nie błąd, to początek SZ, więc false

        // Jeśli żaden z powyższych, to znaczy że to przypadkowa litera (np. 'k' zamiast 'rz')
        return false;
    }

    private void loadNextWord() {
        int lvl = player.getLevel();
        List<Integer> diffs = new ArrayList<>();
        if (activeObject != null && activeObject.getName().equals("BOSS")) diffs.add(3);
        else {
            if (lvl < 3) diffs.add(1);
            else if (lvl < 6) { diffs.add(1); diffs.add(2); }
            else if (lvl < 9) diffs.add(2);
            else { diffs.add(2); diffs.add(3); }
        }
        List<Word> batch = wordBank.getWordsByDifficulty(1, diffs);
        if (!batch.isEmpty()) {
            currentWord = batch.get(0);
            message = "Seria: " + remainingWordsToSolve + " słów do końca.";

            typedAnswer = ""; // <--- DODAJ TO! Resetujemy wpisane litery przy nowym słowie

        } else {
            currentWord = new Word("brak", "b", "Brak słów", 1);
        }
    }

    private void handleTaskSuccess(Player player) {
        if (activeObject == null) return;
        String type = activeObject.getType();
        String name = activeObject.getName();

        if (type.equals("item")) {
            String[] items = {"Mikstura", "Miecz", "Zbroja", "Różdżka"};
            String loot = items[new Random().nextInt(items.length)];
            player.addItem(loot);
            message = "Skrzynia otwarta! Znalazłeś: " + loot;
            activeObject.setActive(false);
        }
        else if (type.equals("obstacle")) {
            activeObject.setActive(false);
            int tX = (int)(activeObject.getX() / TileMap.TILE_SIZE);
            int tY = (int)(activeObject.getY() / TileMap.TILE_SIZE);
            tileMap.setTile(tX, tY, 8);
            message = "Przeszkoda usunięta!";
        }
        else if (type.equals("enemy")) {
            if (name.equals("BOSS")) {
                gameState = GameState.WIN;
            } else {
                activeObject.setActive(false);
                message = "Pokonałeś: " + name;
            }
        }

        if (gameState != GameState.WIN) gameState = GameState.WALKING;
        activeObject = null;
        currentWord = null;
    }

    private boolean checkObjectCollision(Player p) {
        for (GameObject obj : currentObjects) {
            if (obj.isBlocking() && obj.collidesWith(p.getHitboxX(), p.getHitboxY(), p.getHitboxSize())) {
                return true;
            }
        }
        return false;
    }

    private void restartGame() {
        player.reset();
        player.setPosition(50, WINDOW_HEIGHT - 150);
        currentMapX = 0; currentMapY = 0;
        refreshMapData(WINDOW_WIDTH/TileMap.TILE_SIZE, WINDOW_HEIGHT/TileMap.TILE_SIZE);
        gameState = GameState.WALKING;
    }

    private void useItemInCombat(String itemName) {
        if (!player.hasItem(itemName)) return;
        switch (itemName) {
            case "Mikstura":
                if (player.getLives() < 3) { player.useItem(itemName); player.heal(); message = "Uleczono!"; } else message = "Pełne zdrowie!"; break;
            case "Miecz":
                player.useItem(itemName); remainingWordsToSolve = 0; isTaskSolved = true; message = "Cios mieczem! Wygrana! [SPACJA]"; break;
            case "Różdżka":
                player.useItem(itemName); loadNextWord(); message = "Słowo zmienione!"; break;
            case "Zbroja":
                player.useItem(itemName); message = "Zbroja użyta (efekt wizualny)!"; break;
        }
    }

    private void renderGame(GraphicsContext gc, Player player) {
        gc.clearRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        tileMap.render(gc);
        for (GameObject obj : currentObjects) obj.render(gc);

        // TERAZ PLAYER RENDERUJE SIĘ SAM (ANIMACJAMI)
        player.render(gc);

        if (gameState == GameState.DIALOGUE) {
            taskView.render(gc, WINDOW_WIDTH, WINDOW_HEIGHT, currentWord, message, player);
        } else if (gameState == GameState.INVENTORY) {
            inventoryView.render(gc, WINDOW_WIDTH, WINDOW_HEIGHT, player);
        }

        renderHUD(gc, player);
    }

    private void renderHUD(GraphicsContext gc, Player player) {
        // 1. Resetowanie ustawień tekstu (TO NAPRAWIA BŁĄD PRZESUWANIA)
        gc.setTextAlign(javafx.scene.text.TextAlignment.LEFT);
        gc.setTextBaseline(javafx.geometry.VPos.BASELINE);

        // 2. TŁO
        gc.setFill(Color.rgb(0, 0, 0, 0.6));
        gc.fillRoundRect(10, 10, 200, 100, 20, 20);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        // 3. POZIOM
        gc.fillText("Lv " + player.getLevel(), 20, 35);

        // 4. POSTĘP
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        gc.fillText("Postęp: " + player.getProgressString(), 20, 60);

        // 5. SERCA
        int lives = player.getLives();
        for (int i = 0; i < lives; i++) {
            if (heartImg != null) {
                gc.drawImage(heartImg, 20 + (i * 30), 75, 24, 24);
            } else {
                gc.setFill(Color.RED);
                gc.fillOval(20 + (i * 30), 75, 20, 20);
            }
        }

        // 6. Ekwipunek info
        gc.setFill(Color.LIGHTGRAY);
        gc.setFont(Font.font(10));
        gc.fillText("[E] Ekwipunek", 120, 95);
    }

    private int getCurrentMapNumber() {
        int mapWidth = 4;
        return (currentMapY * mapWidth) + currentMapX + 1;
    }

    private void handleMapSwitching(Player player) {
        double safeMargin = 70;
        boolean mapChanged = false;
        if (player.getX() > WINDOW_WIDTH - 40) {
            if (currentMapX < 3) { currentMapX++; player.setPosition(safeMargin, player.getY()); mapChanged = true; }
            else player.setPosition(WINDOW_WIDTH - 40, player.getY());
        } else if (player.getX() < 5) {
            if (currentMapX > 0) { currentMapX--; player.setPosition(WINDOW_WIDTH - safeMargin, player.getY()); mapChanged = true; }
            else player.setPosition(0, player.getY());
        } else if (player.getY() > WINDOW_HEIGHT - 40) {
            if (currentMapY < 3) { currentMapY++; player.setPosition(player.getX(), safeMargin); mapChanged = true; }
            else player.setPosition(player.getX(), WINDOW_HEIGHT - 40);
        } else if (player.getY() < 5) {
            if (currentMapY > 0) { currentMapY--; player.setPosition(player.getX(), WINDOW_HEIGHT - safeMargin); mapChanged = true; }
            else player.setPosition(player.getX(), 0);
        }
        if (mapChanged) {
            refreshMapData(WINDOW_WIDTH/TileMap.TILE_SIZE, WINDOW_HEIGHT/TileMap.TILE_SIZE);
            findSafeEntryPosition(player);
        }
    }

    private void findSafeEntryPosition(Player player) {
        double startX = player.getX();
        double startY = player.getY();
        if (!tileMap.isBlocked(startX, startY, 16)) return;
        for (int range = 1; range <= 10; range++) {
            int off = range * 32;
            if (!tileMap.isBlocked(startX, startY + off, 16)) { player.setPosition(startX, startY + off); return; }
            if (!tileMap.isBlocked(startX, startY - off, 16)) { player.setPosition(startX, startY - off); return; }
            if (!tileMap.isBlocked(startX + off, startY, 16)) { player.setPosition(startX + off, startY); return; }
            if (!tileMap.isBlocked(startX - off, startY, 16)) { player.setPosition(startX - off, startY); return; }
        }
    }

    private void refreshMapData(int cols, int rows) {
        int mapNum = getCurrentMapNumber();
        tileMap.loadMapData(MapDefinitions.getMapData(mapNum, cols, rows));
        currentObjects = MapDefinitions.getObjects(mapNum);
    }

    public static void main(String[] args) {
        launch(args);
    }
}