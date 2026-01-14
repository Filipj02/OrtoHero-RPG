package pl.ortohero.app;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class TileMap {

    public static final int TILE_SIZE = 32;
    private final int cols;
    private final int rows;
    private int[][] tiles;

    // --- MIEJSCE NA GRAFIKI TERENU ---
    private Image grassImg;  // ID 0
    private Image treeImg;   // ID 1
    private Image waterImg;  // ID 2
    private Image wallImg;   // ID 3
    private Image floorImg;  // ID 8 (Podłoga)

    public TileMap(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
        this.tiles = new int[rows][cols];

        loadGraphics();
    }

    private void loadGraphics() {
        try {
            // ODHACZ TE LINIE, GDY BĘDZIESZ MIAŁ PLIKI W /images/
            grassImg = new Image(getClass().getResourceAsStream("/images/grass.png"));
            treeImg = new Image(getClass().getResourceAsStream("/images/tree.png"));
            waterImg = new Image(getClass().getResourceAsStream("/images/water.png"));
            wallImg = new Image(getClass().getResourceAsStream("/images/wall.png"));

            floorImg = new Image(getClass().getResourceAsStream("/images/floor.png")); // <--- ID 8

        } catch (Exception e) {
            System.err.println("Błąd grafiki terenu: " + e.getMessage());
        }
    }

    public void loadMapData(int[][] newMapData) { this.tiles = newMapData; }

    public boolean isBlocked(double px, double py, double size) {
        int left = (int)(px/TILE_SIZE);
        int right = (int)((px+size-1)/TILE_SIZE);
        int top = (int)(py/TILE_SIZE);
        int bottom = (int)((py+size-1)/TILE_SIZE);
        if (left < 0 || top < 0 || right >= cols || bottom >= rows) return true;
        for (int y = top; y <= bottom; y++) {
            for (int x = left; x <= right; x++) {
                int id = tiles[y][x];
                // Blokują: 1, 2, 3. Nie blokują: 0, 8.
                if (id == 1 || id == 2 || id == 3) return true;
            }
        }
        return false;
    }

    public void render(GraphicsContext gc) {
        gc.setImageSmoothing(false);

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                int id = tiles[y][x];
                int rX = x * TILE_SIZE;
                int rY = y * TILE_SIZE;

                // 1. TŁO (Trawa jako baza)
                if (grassImg != null) gc.drawImage(grassImg, rX, rY, TILE_SIZE, TILE_SIZE);
                else { gc.setFill(Color.web("#6abe30")); gc.fillRect(rX, rY, TILE_SIZE, TILE_SIZE); }

                // 2. OBIEKTY
                switch (id) {
                    case 1: // DRZEWO
                        if (treeImg != null) gc.drawImage(treeImg, rX, rY-(treeImg.getHeight()-TILE_SIZE), TILE_SIZE, treeImg.getHeight());
                        else { gc.setFill(Color.SADDLEBROWN); gc.fillRect(rX, rY, TILE_SIZE, TILE_SIZE); }
                        break;

                    case 2: // WODA
                        // Gruntowanie (niebieskie tło pod spodem)
                        gc.setFill(Color.web("#2b65ec")); gc.fillRect(rX, rY, TILE_SIZE, TILE_SIZE);
                        if (waterImg != null) gc.drawImage(waterImg, rX, rY, TILE_SIZE+1, TILE_SIZE+1);
                        break;

                    case 3: // MUR
                        // Rysujemy tło (żeby nie było dziur)
                        gc.setFill(Color.rgb(50, 50, 50));
                        gc.fillRect(rX, rY, TILE_SIZE, TILE_SIZE);

                        if (wallImg != null) {
                            // --- SKALOWANIE ŚCIANY ---
                            double scale = 1;

                            double scaledSize = TILE_SIZE * scale;
                            double offset = (scaledSize - TILE_SIZE) / 2; // O ile przesunąć, żeby było na środku

                            gc.drawImage(wallImg,
                                    rX - offset,
                                    rY - offset,
                                    scaledSize,
                                    scaledSize
                            );
                        } else {
                            // Fallback (brak grafiki)
                            gc.setFill(Color.GRAY);
                            gc.fillRect(rX, rY, TILE_SIZE, TILE_SIZE);
                        }
                        break;

                    case 8: // PODŁOGA (FLOOR)
                        if (floorImg != null) gc.drawImage(floorImg, rX, rY, TILE_SIZE, TILE_SIZE);
                        else {
                            gc.setFill(Color.web("#D2B48C")); // Jasny brąz
                            gc.fillRect(rX, rY, TILE_SIZE, TILE_SIZE);
                            gc.setStroke(Color.rgb(0,0,0,0.1)); // Delikatna ramka
                            gc.strokeRect(rX, rY, TILE_SIZE, TILE_SIZE);
                        }
                        break;
                }
            }
        }
    }
    public void setTile(int x, int y, int newID) {
        if (x >= 0 && x < cols && y >= 0 && y < rows) {
            tiles[y][x] = newID;
        }
    }
}