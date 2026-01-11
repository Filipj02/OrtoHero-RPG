package pl.ortohero.app;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class GameObject {

    private double x, y;
    private double width, height;
    private double scale = 1.0; // NOWOŚĆ: Skala (1.0 = rozmiar gracza 32px)

    private String type; // "enemy", "item", "obstacle"
    private String name;
    private boolean active = true;
    private Image img;

    public GameObject(String name, String type, double x, double y) {
        this.name = name;
        this.type = type;
        this.x = x;
        this.y = y;
        this.width = 32;
        this.height = 32;

        // Domyślne skalowanie dla Bossa
        // --- USTAWIANIE SKALI (WIELKOŚCI) ---
        if (name.equals("BOSS")) {
            setScale(3.0); // Boss jest ogromny (64x64)
        }
        else if (name.equals("Troll")) {
            setScale(2.0); // Troll jest duży (48x48)
        }
        else if (name.equals("Slime")) {
            setScale(0.8); // Slime jest malutki (25x25)
        }
        else if (name.equals("Skrzynia")) setScale(1.2);
        else if (name.equals("Zamknięte Wrota")) setScale(1.1);

        loadGraphics();
    }

    // Metoda do ustawiania wielkości
    public void setScale(double s) {
        this.scale = s;
        this.width = 32 * s;
        this.height = 32 * s;
    }

    private void loadGraphics() {
        try {
            switch (name) {
                // --- PRZESZKODY ---
                case "Zawalony Most": img = new Image(getClass().getResourceAsStream("/images/bridge.png"));
                    break; // ID 6
                case "Zamknięte Wrota": img = new Image(getClass().getResourceAsStream("/images/gate.png"));
                    break; // ID 7

                // --- ITEMY ---
                case "Skrzynia": img = new Image(getClass().getResourceAsStream("/images/chest.png"));
                    break; // ID 4

                // --- POTWORY ---
                case "Goblin": // ID 51
                    img = new Image(getClass().getResourceAsStream("/images/orc_new.png"));
                    break; // 51
                case "Wilk": // ID 52
                    img = new Image(getClass().getResourceAsStream("/images/wolf.png"));
                    break; // 52
                case "Beast": img = new Image(getClass().getResourceAsStream("/images/beast.png")); break; // 53
                case "Troll": // ID 54
                    img = new Image(getClass().getResourceAsStream("/images/troll.png"));
                    // Trola można powiększyć w konstruktorze, ale tu ładujemy tylko plik
                    break; // 54 (NOWY)
                case "BOSS":  img = new Image(getClass().getResourceAsStream("/images/boss.png")); break; // 55
                case "Slime": // ID 56
                    img = new Image(getClass().getResourceAsStream("/images/slime.png"));
                    break;// 56 (NOWY)
                case "Nieznany": img = new Image(getClass().getResourceAsStream("/images/titan_new.png")); break; // 57 (NOWY)
            }
        } catch (Exception e) {}
    }

    public void render(GraphicsContext gc) {
        if (!active) return;

        if (img != null) {
            gc.drawImage(img, x, y, width, height);
        } else {
            // FALLBACK (Brak grafiki)
            switch (type) {
                case "enemy" -> {
                    if (name.equals("BOSS")) gc.setFill(Color.DARKRED);
                    else if (name.equals("Slime")) gc.setFill(Color.LIGHTGREEN);
                    else gc.setFill(Color.RED); // Domyślny potwór = Czerwony blok
                }
                case "item" -> gc.setFill(Color.PURPLE);
                case "obstacle" -> gc.setFill(Color.BLACK); // Most/Brama
                default -> gc.setFill(Color.WHITE);
            }
            gc.fillRect(x, y, width, height);
            gc.setStroke(Color.BLACK);
            gc.strokeRect(x, y, width, height);
        }
    }

    // Czy obiekt blokuje fizycznie przejście?
    public boolean isBlocking() {
        if (!active) return false;
        // Potwory i Przeszkody blokują. Skrzynie NIE blokują.
        return type.equals("enemy") || type.equals("obstacle");
    }

    // Kolizja z graczem (do blokowania ruchu)
    public boolean collidesWith(double px, double py, double pSize) {
        if (!active) return false;
        return px < x + width && px + pSize > x &&
                py < y + height && py + pSize > y;
    }

    // Zbliżenie do interakcji (SPACJA)
    public boolean isPlayerClose(Player player) {
        if (!active) return false;
        double cx = x + width/2;
        double cy = y + height/2;
        double px = player.getHitboxX() + player.getHitboxSize()/2;
        double py = player.getHitboxY() + player.getHitboxSize()/2;
        // Zwiększamy zasięg dla dużych potworów
        return Math.sqrt(Math.pow(px-cx, 2) + Math.pow(py-cy, 2)) < (40 + width/2);
    }

    public String getName() { return name; }
    public String getType() { return type; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    // Gettery do pozycji (potrzebne do podmiany kafelków)
    public double getX() { return x; }
    public double getY() { return y; }
}