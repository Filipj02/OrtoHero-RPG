package pl.ortohero.app;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class TileMap {

    public static final int TILE_SIZE = 32;
    private final int cols;
    private final int rows;

    // Tablica przechowująca ID kafelków
    private int[][] tiles;

    public TileMap(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
        this.tiles = new int[rows][cols];

        // Domyślnie ładujemy pustą mapę lub pierwszą z definicji
        // (Zostanie to nadpisane w MainApp, ale warto zainicjować)
        loadMapData(MapDefinitions.getMap1(cols, rows));
    }

    // Metoda do zmiany mapy "w locie"
    public void loadMapData(int[][] newMapData) {
        this.tiles = newMapData;
    }

    // Sprawdzanie kolizji
    // px, py - pozycja gracza (hitboxa)
    // size - rozmiar hitboxa
    public boolean isBlocked(double px, double py, double size) {
        // Obliczamy, które kafelki zajmuje hitbox gracza (lewy-góra, prawy-dół)
        int leftTile   = (int) (px / TILE_SIZE);
        int rightTile  = (int) ((px + size - 1) / TILE_SIZE);
        int topTile    = (int) (py / TILE_SIZE);
        int bottomTile = (int) ((py + size - 1) / TILE_SIZE);

        // Zabezpieczenie przed wyjściem indeksu poza tablicę
        if (leftTile < 0 || topTile < 0 || rightTile >= cols || bottomTile >= rows) {
            // Jeśli hitbox wystaje poza świat, traktujemy to jak blokadę (opcjonalne)
            return true;
        }

        // Sprawdzamy każdy kafelek, który dotyka hitbox gracza
        for (int ty = topTile; ty <= bottomTile; ty++) {
            for (int tx = leftTile; tx <= rightTile; tx++) {
                int tileID = tiles[ty][tx];

                // JEŚLI ID > 0, TO JEST PRZESZKODA
                if (tileID > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public void render(GraphicsContext gc) {
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                int tileID = tiles[y][x];

                // Ustawiamy kolor w zależności od ID
                switch (tileID) {
                    case 1 -> gc.setFill(Color.SADDLEBROWN); // Drzewo
                    case 2 -> gc.setFill(Color.ROYALBLUE);   // Woda
                    case 3 -> gc.setFill(Color.GRAY);        // Mur
                    default -> gc.setFill(Color.LIGHTGREEN); // Trawa (0)
                }

                gc.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);

                // Opcjonalnie: Siatka (grid) dla ułatwienia testów
                // gc.setStroke(Color.BLACK);
                // gc.strokeRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }
    }
}