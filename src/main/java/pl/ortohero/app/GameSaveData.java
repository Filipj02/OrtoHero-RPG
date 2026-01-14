package pl.ortohero.app;

import java.util.List;
import java.util.Map;

public class GameSaveData {
    // DANE GRACZA
    public int level;
    public int lives;
    public int wordsSolved;
    public double playerX, playerY;
    public Map<String, Integer> inventory;

    // POZYCJA NA MAPIE ŚWIATA
    public int currentMapX;
    public int currentMapY;

    // STAN ŚWIATA (Zapisujemy mapy, które gracz już odwiedził i zmienił)
    public Map<Integer, int[][]> savedTiles;
    public Map<Integer, List<GameObject>> savedObjects;
}