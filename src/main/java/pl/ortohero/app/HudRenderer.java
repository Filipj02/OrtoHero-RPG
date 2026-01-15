package pl.ortohero.app;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class HudRenderer {

    private Image heartImg;
    private Font dyslexiaFont;

    public HudRenderer() {
        try {
            heartImg = new Image(getClass().getResourceAsStream("/images/heart.png"));
            dyslexiaFont = Font.loadFont(getClass().getResourceAsStream("/fonts/OpenDyslexic-Regular.otf"), 20);
        } catch (Exception e) {
            System.err.println("Błąd ładowania grafik HUD: " + e.getMessage());
        }
    }

    public void render(GraphicsContext gc, Player player) {
        // Reset ustawień
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(javafx.geometry.VPos.BASELINE);

        // Tło
        gc.setFill(Color.rgb(0, 0, 0, 0.6));
        gc.fillRoundRect(10, 10, 220, 100, 20, 20);

        gc.setFill(Color.WHITE);

        // Czcionka
        if (dyslexiaFont != null) {
            gc.setFont(Font.font(dyslexiaFont.getFamily(), 18));
        } else {
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        }

        // Teksty
        gc.fillText("Lv " + player.getLevel(), 20, 35);
        gc.fillText("Postęp: " + player.getProgressString(), 20, 60);

        // Serca
        int lives = player.getLives();
        for (int i = 0; i < lives; i++) {
            if (heartImg != null) {
                gc.drawImage(heartImg, 20 + (i * 30), 75, 24, 24);
            } else {
                gc.setFill(Color.RED);
                gc.fillOval(20 + (i * 30), 75, 20, 20);
            }
        }



        // Info o ekwipunku
        gc.setFill(Color.LIGHTGRAY);
        gc.setFont(Font.font(10));
        gc.fillText("[E] Ekwipunek", 120, 95);
    }


    public void renderPauseOverlay(GraphicsContext gc, double width, double height) {
        gc.setFill(Color.rgb(0, 0, 0, 0.5));
        gc.fillRect(0, 0, width, height);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 40));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("PAUZA", width / 2, 150);
    }
}