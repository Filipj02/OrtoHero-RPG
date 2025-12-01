package pl.ortohero.app;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class TileMap {

    public static final int TILE_SIZE = 32;
    private final int cols;
    private final int rows;
    //private final int mapNumber;
    private final int[][] tiles; // 0 – trawa (przejście), 1 – blok (ściana)

    public TileMap(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
        this.tiles = new int[rows][cols];

        // 1) DOMYŚLNIE WSZĘDZIE TRAWNIK (0)
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                tiles[y][x] = 0;
            }
        }

        // 2) TU DODAJESZ BLOKUJĄCE POLA (1)
        // prostokąt 2x2 w środku mapy
        tiles[5][5] = 1;
        tiles[5][6] = 1;
        tiles[6][5] = 1;
        tiles[6][6] = 1;
    }

    public boolean isBlocked(double px, double py, double size) {
        int leftTile   = (int) (px / TILE_SIZE);
        int rightTile  = (int) ((px + size - 1) / TILE_SIZE);
        int topTile    = (int) (py / TILE_SIZE);
        int bottomTile = (int) ((py + size - 1) / TILE_SIZE);

        if (leftTile < 0 || topTile < 0 || rightTile >= cols || bottomTile >= rows) {
            return true; // poza mapą = blok
        }

        for (int ty = topTile; ty <= bottomTile; ty++) {
            for (int tx = leftTile; tx <= rightTile; tx++) {
                if (tiles[ty][tx] == 1) {
                    return true;
                }
            }
        }
        return false;
    }

    public void render(GraphicsContext gc) {
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                if (tiles[y][x] == 0) {
                    gc.setFill(Color.LIGHTGREEN); // trawa
                } else {
                    gc.setFill(Color.DARKOLIVEGREEN); // blok – inny zielony
                }
                gc.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }
    }
}

