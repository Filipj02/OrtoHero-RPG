package pl.ortohero.app;

public class MapDefinitions {

    // 0 = Trawa, 1 = Drzewo, 2 = Woda, 3 = Mur

    public static int[][] getMap1(int cols, int rows) {
        int[][] map = new int[rows][cols];
        // Domyślnie trawa
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) map[y][x] = 0;
        }

        // Drzewa i woda (bez zmian)
        map[5][5] = 1;
        map[5][6] = 1;
        for(int x=0; x<cols; x++) map[10][x] = 2;
        map[10][10] = 0; // Mostek

        // WAŻNE: Upewnij się, że prawa krawędź (przejście do Mapy 2) jest pusta
        // Tutaj jest trawa, więc jest OK.
        return map;
    }

    public static int[][] getMap2(int cols, int rows) {
        int[][] map = new int[rows][cols];

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                // Ramka z muru (3)
                if (y == 0 || y == rows - 1 || x == 0 || x == cols - 1) {
                    map[y][x] = 3;
                } else {
                    map[y][x] = 0;
                }
            }
        }

        // --- TWORZENIE PRZEJŚĆ (DZIURY W MURZE) ---

        // Dziura z LEWEJ (żeby wejść z Mapy 1)
        // Robimy szerokie przejście na środku wysokości (np. y od 8 do 12)
        for(int y = 8; y < 13; y++) {
            map[y][0] = 0;
        }

        // Przeszkoda na środku
        map[7][7] = 3;

        return map;
    }
    // ... (Twoje metody getMap1, getMap2) ...

    // NOWA METODA: Zwraca listę obiektów dla danej mapy
    public static java.util.List<GameObject> getObjects(int mapNumber) {
        java.util.List<GameObject> list = new java.util.ArrayList<>();

        if (mapNumber == 1) {
            // Mapa 1: Tabliczka przy moście i Goblin w lesie
            // Współrzędne podajemy w pikselach: Tile_X * 32, Tile_Y * 32
            list.add(new GameObject("Stara Tablica", "sign", 9 * 32, 9 * 32));
            list.add(new GameObject("Zły Goblin", "enemy", 20 * 32, 5 * 32));
        } else if (mapNumber == 2) {
            // Mapa 2: Strażnik muru
            list.add(new GameObject("Strażnik Muru", "enemy", 8 * 32, 7 * 32));
        }

        return list;
    }
}