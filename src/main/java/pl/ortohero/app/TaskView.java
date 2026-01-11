package pl.ortohero.app;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import java.util.Map;

public class TaskView {

    private static final Color BG_COLOR = Color.rgb(40, 40, 60, 0.95);
    private static final Color ACCENT_COLOR = Color.GOLD;

    // Grafiki (te same co w Inventory)
    private Image potionImg, swordImg, armorImg, wandImg;

    public TaskView() {
        loadItemGraphics();
    }

    private void loadItemGraphics() {
        try {
            // Odkomentuj jak będziesz miał pliki
             potionImg = new Image(getClass().getResourceAsStream("/images/potion.png"));
             swordImg = new Image(getClass().getResourceAsStream("/images/sword.png"));
             armorImg = new Image(getClass().getResourceAsStream("/images/chain.png"));
            wandImg = new Image(getClass().getResourceAsStream("/images/staff.png"));
            // featherImg = new Image(getClass().getResourceAsStream("/images/feather.png"));
        } catch (Exception e) {}
    }

    public void render(GraphicsContext gc, double width, double height, Word currentWord, String message, Player player) {
        // TŁO i RAMKA (Bez zmian)
        double marginX = 200, marginY = 100;
        double boxWidth = width - (2 * marginX);
        double boxHeight = height - (2 * marginY);

        gc.setFill(BG_COLOR);
        gc.fillRoundRect(marginX, marginY, boxWidth, boxHeight, 30, 30);
        gc.setStroke(ACCENT_COLOR);
        gc.setLineWidth(3);
        gc.strokeRoundRect(marginX, marginY, boxWidth, boxHeight, 30, 30);

        if (currentWord == null) return;

        // TEKSTY (Bez zmian)
        gc.setFill(ACCENT_COLOR);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Verdana", FontWeight.BOLD, 28));
        gc.fillText("ZADANIE ORTOGRAFICZNE", width / 2, marginY + 60);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Courier New", FontWeight.BOLD, 48));
        gc.fillText(currentWord.getMaskedWord(), width / 2, height / 2 - 20);

        gc.setFont(Font.font("Arial", 20));
        if (message != null && !message.isEmpty()) {
            if (message.startsWith("BRAWO")) gc.setFill(Color.LIGHTGREEN);
            else if (message.startsWith("BŁĄD") || message.startsWith("GAME OVER")) gc.setFill(Color.SALMON);
            else gc.setFill(Color.LIGHTGRAY);
            gc.fillText(message, width / 2, height / 2 + 50);
        } else {
            gc.setFill(Color.LIGHTGRAY);
            gc.fillText("Wpisz brakującą literę...", width / 2, height / 2 + 50);
        }

        drawItemBar(gc, width, height, player);
    }

    private final String[] possibleItems = {"Mikstura", "Miecz", "Różdżka", "Pióro", "Zbroja"};
    private final double itemWidth = 80;
    private final double itemHeight = 80; // Zwiększone dla ikony

    private void drawItemBar(GraphicsContext gc, double width, double height, Player player) {
        double barY = height - 130;
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.setTextAlign(TextAlignment.CENTER);

        Map<String, Integer> inv = player.getInventory();
        int index = 0;

        for (String item : possibleItems) {
            if (inv.containsKey(item) && inv.get(item) > 0) {
                double centerX = width / 2 - 200 + (index * 100);
                double rectX = centerX - (itemWidth / 2);

                // Tło przycisku
                gc.setFill(Color.rgb(80, 80, 80));
                gc.fillRoundRect(rectX, barY, itemWidth, itemHeight, 10, 10);
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(1);
                gc.strokeRoundRect(rectX, barY, itemWidth, itemHeight, 10, 10);

                // --- RYSOWANIE OBRAZKA ---
                Image imgToDraw = null;
                switch(item) {
                    case "Mikstura" -> imgToDraw = potionImg;
                    case "Miecz" -> imgToDraw = swordImg;
                    case "Zbroja" -> imgToDraw = armorImg;
                    case "Różdżka" -> imgToDraw = wandImg;
                    //case "Pióro" -> imgToDraw = featherImg;
                }

                if (imgToDraw != null) {
                    gc.drawImage(imgToDraw, rectX + 16, barY + 10, 48, 48);
                } else {
                    // Tekst zastępczy
                    gc.setFill(Color.GOLD);
                    gc.fillText(item, centerX, barY + 30);
                }

                // Ilość
                gc.setFill(Color.WHITE);
                gc.fillText("x" + inv.get(item), centerX, barY + itemHeight - 10);
            }
            index++;
        }

        gc.setFill(Color.GRAY);
        gc.setFont(Font.font("Arial", 12));
        gc.fillText("[ESC] Ucieczka", width / 2, height - 20);
    }

    // Metoda handleClick pozostaje bez zmian (skopiuj ją z poprzedniej wersji jeśli zniknęła)
    public String handleClick(double mouseX, double mouseY, double width, double height, Player player) {
        double barY = height - 130;
        Map<String, Integer> inv = player.getInventory();
        int index = 0;
        for (String item : possibleItems) {
            if (inv.containsKey(item) && inv.get(item) > 0) {
                double centerX = width / 2 - 200 + (index * 100);
                double rectX = centerX - (itemWidth / 2);
                if (mouseX >= rectX && mouseX <= rectX + itemWidth && mouseY >= barY && mouseY <= barY + itemHeight) {
                    return item;
                }
            }
            index++;
        }
        return null;
    }
}