package pl.ortohero.app;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class GameObject {

    private double x, y;
    private double width, height;
    private String type; // np. "enemy", "sign", "item"
    private String name; // np. "Goblin", "Tabliczka"
    private boolean active = true; // Czy obiekt istnieje (czy nie został pokonany)

    public GameObject(String name, String type, double x, double y) {
        this.name = name;
        this.type = type;
        this.x = x;
        this.y = y;
        this.width = 32; // Domyślny rozmiar 1 kafelka
        this.height = 32;
    }

    public void render(GraphicsContext gc) {
        if (!active) return;

        // Tymczasowa grafika: Czerwony kwadrat dla wroga, Żółty dla tabliczki
        if (type.equals("enemy")) {
            gc.setFill(Color.RED);
        } else if (type.equals("sign")) {
            gc.setFill(Color.GOLD);
        } else {
            gc.setFill(Color.WHITE);
        }

        gc.fillRect(x, y, width, height);

        // Obramowanie
        gc.setStroke(Color.BLACK);
        gc.strokeRect(x, y, width, height);
    }

    // Sprawdza, czy gracz jest wystarczająco blisko, żeby wejść w interakcję
    public boolean isPlayerClose(Player player) {
        if (!active) return false;

        double playerCenterX = player.getX() + player.getHitboxSize() / 2;
        double playerCenterY = player.getY() + player.getHitboxSize() / 2;

        double objectCenterX = x + width / 2;
        double objectCenterY = y + height / 2;

        double distance = Math.sqrt(Math.pow(playerCenterX - objectCenterX, 2) + Math.pow(playerCenterY - objectCenterY, 2));

        // Jeśli odległość jest mniejsza niż 50 pikseli -> interakcja możliwa
        return distance < 50;
    }

    public String getName() { return name; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}