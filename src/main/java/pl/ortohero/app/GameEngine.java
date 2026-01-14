package pl.ortohero.app;

import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import java.util.Random;

// Import statyczny dla wygody (E, SPACE, ESCAPE...)
import static javafx.scene.input.KeyCode.*;

public class GameEngine {

    // --- STANY ---
    public enum GameState { WALKING, DIALOGUE, INVENTORY, WIN, MENU }
    private GameState gameState = GameState.WALKING;

    // --- MODUŁY ---
    private WorldManager worldManager;
    private CombatSystem combatSystem;
    private SoundManager soundManager;
    private HudRenderer hudRenderer;
    private TaskView taskView;
    private InventoryView inventoryView;
    private Player player;

    // --- STEROWANIE ---
    private boolean up, down, left, right, spacePressed, spaceWasProcessed, eKeyWasProcessed;
    private VBox menuBox; // Referencja do menu, żeby je ukrywać/pokazywać

    public GameEngine(VBox menuBox) {
        this.menuBox = menuBox;
        initModules();
    }

    private void initModules() {
        // 1. Najpierw dźwięk
        soundManager = new SoundManager();
        soundManager.initSounds(); // Warto od razu załadować pliki

        // 2. Potem inne rzeczy
        player = new Player();
        worldManager = new WorldManager();

        // 3. CombatSystem dostaje soundManager (TU NIE MOŻE BYĆ PUSTY NAWIAS!)
        combatSystem = new CombatSystem(soundManager);

        taskView = new TaskView();
        inventoryView = new InventoryView();
        hudRenderer = new HudRenderer();

        // ... reszta ładowania gry ...
        if (!loadGameLogic()) {
            player.setPosition(50, MainApp.WINDOW_HEIGHT - 150);
        }
        worldManager.refreshMapData(MainApp.WINDOW_WIDTH, MainApp.WINDOW_HEIGHT);
    }

    // --- GŁÓWNA PĘTLA LOGIKI (UPDATE) ---
    public void update() {
        if (gameState == GameState.MENU) return;

        if (gameState == GameState.WALKING) {
            // 1. Zapamiętujemy, gdzie staliśmy przed ruchem (stara, bezpieczna pozycja)
            double oldX = player.getX();
            double oldY = player.getY();

            // 2. Gracz oblicza nową pozycję na podstawie klawiszy (gdzie CHCE iść)
            player.update(up, down, left, right);
            double intendedX = player.getX();
            double intendedY = player.getY();

            // --- SPRAWDZANIE OSI X (POZIOM) ---
            // Ustawiamy gracza na nowym X, ale STARYM Y (żeby sprawdzić tylko bok)
            player.setPosition(intendedX, oldY);

            // Jeśli weszliśmy w ścianę...
            if (worldManager.checkCollision(player)) {
                player.setPosition(oldX, oldY); // ...cofamy X do starego
            }
            // Zapamiętujemy zaakceptowany X (nowy lub stary, jeśli była ściana)
            double acceptedX = player.getX();


            // --- SPRAWDZANIE OSI Y (PION) ---
            // Ustawiamy gracza na zaakceptowanym X i nowym, CHCIANYM Y
            player.setPosition(acceptedX, intendedY);

            // Jeśli weszliśmy w ścianę...
            if (worldManager.checkCollision(player)) {
                player.setPosition(acceptedX, oldY); // ...cofamy Y do starego
            }

            // --- RESZTA LOGIKI (ZMIANA MAPY, INTERAKCJE) ---
            worldManager.handleMapSwitching(player, MainApp.WINDOW_WIDTH, MainApp.WINDOW_HEIGHT);

            if (spacePressed && !spaceWasProcessed) {
                GameObject obj = worldManager.checkInteraction(player);
                if (obj != null) interact(obj);
                spaceWasProcessed = true;
            }
        }
        else if (gameState == GameState.DIALOGUE) {
            if (spacePressed && !spaceWasProcessed) {
                if (combatSystem.isTaskSolved()) {
                    if (player.getLives() <= 0) restartGame();
                    else handleWin();
                }
                spaceWasProcessed = true;
            }
        }
        else if (gameState == GameState.WIN && spacePressed && !spaceWasProcessed) {
            restartGame();
            spaceWasProcessed = true;
        }
    }

    // --- GŁÓWNA PĘTLA RYSOWANIA (RENDER) ---
    public void render(GraphicsContext gc) {
        gc.clearRect(0, 0, MainApp.WINDOW_WIDTH, MainApp.WINDOW_HEIGHT);
        worldManager.render(gc);
        player.render(gc);

        if (gameState == GameState.DIALOGUE) {
            taskView.render(gc, MainApp.WINDOW_WIDTH, MainApp.WINDOW_HEIGHT, combatSystem.getCurrentWord(),
                    combatSystem.getMessage(), player, combatSystem.isShowingError());
        } else if (gameState == GameState.INVENTORY) {
            inventoryView.render(gc, MainApp.WINDOW_WIDTH, MainApp.WINDOW_HEIGHT, player);
        }

        hudRenderer.render(gc, player);


        if (gameState == GameState.MENU) {
            hudRenderer.renderPauseOverlay(gc, MainApp.WINDOW_WIDTH, MainApp.WINDOW_HEIGHT);
        }
    }

    // --- OBSŁUGA WEJŚCIA (PRZEKAZYWANA Z MAIN) ---
    public void handleKeyPressed(KeyEvent e) {
        KeyCode code = e.getCode();

        if (gameState == GameState.WALKING) {
            if (code == UP || code == W) up = true;
            if (code == DOWN || code == S) down = true;
            if (code == LEFT || code == A) left = true;
            if (code == RIGHT || code == D) right = true;
            if (code == SPACE) spacePressed = true;
            if (code == E && !eKeyWasProcessed) { gameState = GameState.INVENTORY; eKeyWasProcessed = true; }
            if (code == ESCAPE) openMenu();
        }
        else if (gameState == GameState.MENU) {
            if (code == ESCAPE) closeMenu();
        }
        else if (gameState == GameState.INVENTORY) {
            if (code == E || code == ESCAPE) gameState = GameState.WALKING;
        }
        else if (gameState == GameState.DIALOGUE) {
            if (code == ESCAPE) { gameState = GameState.WALKING; combatSystem.setActiveObject(null); }
            if (combatSystem.isShowingError() && code == SPACE) {
                combatSystem.setShowingError(false);
                combatSystem.loadNextWord(player.getLevel());
                return;
            }
            if (combatSystem.isTaskSolved() && code == SPACE) spacePressed = true;
        }
        else if (gameState == GameState.WIN && code == SPACE) spacePressed = true;
    }

    public void handleKeyReleased(KeyEvent e) {
        KeyCode code = e.getCode();
        if (code == UP || code == W) up = false;
        if (code == DOWN || code == S) down = false;
        if (code == LEFT || code == A) left = false;
        if (code == RIGHT || code == D) right = false;
        if (code == SPACE) { spacePressed = false; spaceWasProcessed = false; }
        if (code == E) eKeyWasProcessed = false;
    }

    public void handleKeyTyped(String charInput) {
        if (gameState == GameState.DIALOGUE && !combatSystem.isTaskSolved() && !combatSystem.isShowingError()) {
            combatSystem.handleTyping(charInput, player);
        }
    }

    public void handleMouseClick(double x, double y) {
        if (gameState == GameState.DIALOGUE && !combatSystem.isTaskSolved()) {
            String item = taskView.handleClick(x, y, MainApp.WINDOW_WIDTH, MainApp.WINDOW_HEIGHT, player);
            if (item != null) useItem(item);
        }
    }

    // --- METODY POMOCNICZE (LOGIKA) ---

    private void interact(GameObject obj) {
        if (obj.getName().equals("BOSS") && player.getLevel() < 10) return; // Blokada lvl
        combatSystem.startCombat(obj, player.getLevel());
        gameState = GameState.DIALOGUE;
        player.resetAnimationState();
        up = false; down = false; left = false; right = false;
    }

    private void handleWin() {
        GameObject obj = combatSystem.getActiveObject();
        if (obj == null) return;

        if (obj.getType().equals("item")) {
            String[] items = {"Mikstura", "Miecz", "Zbroja", "Różdżka"};
            player.addItem(items[new Random().nextInt(items.length)]);
            obj.setActive(false);
        } else if (obj.getType().equals("obstacle") || obj.getType().equals("enemy")) {
            if (obj.getName().equals("BOSS")) {
                gameState = GameState.WIN;
                soundManager.playWin(); // <--- FANFARY!
            }
            else {
                obj.setActive(false);
            }
            if (obj.getType().equals("obstacle")) {
                int tx = (int) obj.getX() / 32; int ty = (int) obj.getY() / 32;
                worldManager.getTileMap().setTile(tx, ty, 8);
            }
        }
        if (gameState != GameState.WIN) gameState = GameState.WALKING;
        combatSystem.setActiveObject(null);
    }

    private void useItem(String item) {
        if (!player.hasItem(item)) return;
        if (item.equals("Mikstura") && player.getLives() < 3) { player.useItem(item); player.heal(); }
        else if (item.equals("Różdżka")) { player.useItem(item); combatSystem.loadNextWord(player.getLevel()); }
        else if  (item.equals("Miecz")) { player.useItem(item); combatSystem.useSwordEffect(); }
        else if (item.equals("Zbroja")) { player.useItem(item); player.equipArmor();

        }
    }

    private void openMenu() {
        gameState = GameState.MENU;
        menuBox.setVisible(true);
    }

    private void closeMenu() {
        gameState = GameState.WALKING;
        menuBox.setVisible(false);
    }

    private boolean loadGameLogic() {
        GameSaveData data = SaveManager.loadSaveFile();
        if (data == null) return false;
        MapDefinitions.resetAllMaps();
        if (data.savedTiles != null) MapDefinitions.setCachedTiles(data.savedTiles);
        if (data.savedObjects != null) {
            MapDefinitions.setCachedObjects(data.savedObjects);
            SaveManager.restoreGraphics(data.savedObjects);
        }
        player.reset();
        for (int i = 1; i < data.level; i++) player.addSuccess();
        player.setWordsSolvedInCurrentLevel(data.wordsSolved);
        while (player.getLives() > data.lives) player.loseLife();
        player.setPosition(data.playerX, data.playerY);
        if (data.inventory != null) for (var e : data.inventory.entrySet()) for (int i = 0; i < e.getValue(); i++) player.addItem(e.getKey());
        worldManager.setMapPosition(data.currentMapX, data.currentMapY);
        return true;
    }

    private void restartGame() {
        soundManager.restartMusic();

        SaveManager.deleteSave();
        MapDefinitions.resetAllMaps();
        player.reset();
        player.setPosition(50, MainApp.WINDOW_HEIGHT - 150);
        worldManager.reset();
        worldManager.refreshMapData(MainApp.WINDOW_WIDTH, MainApp.WINDOW_HEIGHT);
        gameState = GameState.WALKING;
    }


    public void saveAndExit() {
        SaveManager.saveGame(player, worldManager.getMapX(), worldManager.getMapY());
        Platform.exit();
        System.exit(0);
    }

    public SoundManager getSoundManager() {
        return soundManager;
    }
}