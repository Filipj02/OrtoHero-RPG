package pl.ortohero.app;

import javafx.scene.canvas.GraphicsContext;
import java.util.ArrayList;
import java.util.List;

public class WorldManager {

    private TileMap tileMap;
    private List<GameObject> currentObjects = new ArrayList<>();
    private int mapX = 0;
    private int mapY = 0;

    public void refreshMapData(int winWidth, int winHeight) {
        int cols = winWidth / TileMap.TILE_SIZE;
        int rows = winHeight / TileMap.TILE_SIZE;
        if (tileMap == null) tileMap = new TileMap(cols, rows);

        int mapNum = (mapY * 3) + mapX + 1; // Logika dla mapy 3x3
        tileMap.loadMapData(MapDefinitions.getMapData(mapNum, cols, rows));
        currentObjects = MapDefinitions.getObjects(mapNum);
    }

    public void render(GraphicsContext gc) {
        tileMap.render(gc);
        for (GameObject obj : currentObjects) obj.render(gc);
    }

    // Obsługa zmiany mapy (krawędzie ekranu)
    public void handleMapSwitching(Player player, int winWidth, int winHeight) {
        double safeMargin = 70;
        boolean mapChanged = false;

        if (player.getX() > winWidth - 40) {
            if (mapX < 2) { mapX++; player.setPosition(safeMargin, player.getY()); mapChanged = true; }
            else player.setPosition(winWidth - 40, player.getY());
        } else if (player.getX() < 5) {
            if (mapX > 0) { mapX--; player.setPosition(winWidth - safeMargin, player.getY()); mapChanged = true; }
            else player.setPosition(0, player.getY());
        } else if (player.getY() > winHeight - 40) {
            if (mapY < 2) { mapY++; player.setPosition(player.getX(), safeMargin); mapChanged = true; }
            else player.setPosition(player.getX(), winHeight - 40);
        } else if (player.getY() < 5) {
            if (mapY > 0) { mapY--; player.setPosition(player.getX(), winHeight - safeMargin); mapChanged = true; }
            else player.setPosition(player.getX(), 0);
        }

        if (mapChanged) {
            refreshMapData(winWidth, winHeight);
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

    public boolean checkCollision(Player p) {
        // Kolizja ze ścianami
        if (tileMap.isBlocked(p.getHitboxX(), p.getHitboxY(), p.getHitboxSize())) return true;
        // Kolizja z obiektami (skrzynie, wrogowie)
        for (GameObject obj : currentObjects) {
            if (obj.isBlocking() && obj.collidesWith(p.getHitboxX(), p.getHitboxY(), p.getHitboxSize())) return true;
        }
        return false;
    }

    public GameObject checkInteraction(Player player) {
        for (GameObject obj : currentObjects) {
            if (obj.isPlayerClose(player)) return obj;
        }
        return null;
    }

    public void reset() {
        mapX = 0;
        mapY = 0;
    }

    // Settery/Gettery dla zapisu gry
    public int getMapX() { return mapX; }
    public int getMapY() { return mapY; }
    public void setMapPosition(int x, int y) { this.mapX = x; this.mapY = y; }
    public TileMap getTileMap() { return tileMap; } // Opcjonalnie
}