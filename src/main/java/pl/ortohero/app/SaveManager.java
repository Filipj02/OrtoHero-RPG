package pl.ortohero.app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class SaveManager {

    private static final String SAVE_FILE = "save.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void saveGame(Player player, int mapX, int mapY) {
        try {
            GameSaveData data = new GameSaveData();

            // Dane gracza
            data.level = player.getLevel();
            data.lives = player.getLives();
            data.wordsSolved = player.getWordsSolvedInCurrentLevel();
            data.playerX = player.getX();
            data.playerY = player.getY();
            data.inventory = player.getInventory();

            // Mapa
            data.currentMapX = mapX;
            data.currentMapY = mapY;

            // Świat
            data.savedTiles = MapDefinitions.getCachedTiles();
            data.savedObjects = MapDefinitions.getCachedObjects();

            Writer writer = new FileWriter(SAVE_FILE);
            gson.toJson(data, writer);
            writer.close();
            System.out.println("Gra zapisana!");

        } catch (Exception e) {
            System.err.println("Błąd zapisu: " + e.getMessage());
        }
    }

    public static GameSaveData loadSaveFile() {
        try {
            if (!Files.exists(Paths.get(SAVE_FILE))) return null;

            String json = new String(Files.readAllBytes(Paths.get(SAVE_FILE)));
            return gson.fromJson(json, GameSaveData.class);
        } catch (Exception e) {
            System.err.println("Błąd odczytu: " + e.getMessage());
            return null;
        }
    }

    public static void deleteSave() {
        try {
            Files.deleteIfExists(Paths.get(SAVE_FILE));
            System.out.println("Zapis usunięty.");
        } catch (Exception e) {
            System.err.println("Błąd usuwania: " + e.getMessage());
        }
    }

    // Metoda pomocnicza do przywracania grafik po wczytaniu
    public static void restoreGraphics(Map<Integer, List<GameObject>> savedObjects) {
        if (savedObjects != null) {
            for (List<GameObject> list : savedObjects.values()) {
                for (GameObject obj : list) {
                    obj.loadGraphics();
                }
            }
        }
    }
}