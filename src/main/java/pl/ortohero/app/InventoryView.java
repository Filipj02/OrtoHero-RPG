package pl.ortohero.app;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.Map;

public class InventoryView {

    // Grafiki przedmiotów
    private Image potionImg, swordImg, armorImg, wandImg;

    public InventoryView() {
        loadItemGraphics();
    }

    private void loadItemGraphics() {
        try {
            // Upewnij się, że masz te pliki w folderze images!
            // Możesz zakomentować te linie, jeśli jeszcze nie masz plików
            potionImg = new Image(getClass().getResourceAsStream("/images/potion.png"));
            swordImg = new Image(getClass().getResourceAsStream("/images/sword.png"));
             armorImg = new Image(getClass().getResourceAsStream("/images/chain.png"));
            wandImg = new Image(getClass().getResourceAsStream("/images/staff.png"));
            // featherImg = new Image(getClass().getResourceAsStream("/images/feather.png"));
        } catch (Exception e) {
            System.err.println("Błąd ładowania ikon ekwipunku: " + e.getMessage());
        }
    }

    public void render(GraphicsContext gc, double width, double height, Player player) {
        // 1. Tło (przyciemnienie)
        gc.setFill(Color.rgb(0, 0, 0, 0.7));
        gc.fillRect(0, 0, width, height);

        // 2. Okno
        double boxW = 600;
        double boxH = 400;
        double x = (width - boxW) / 2;
        double y = (height - boxH) / 2;

        gc.setFill(Color.rgb(60, 40, 30));
        gc.fillRoundRect(x, y, boxW, boxH, 20, 20);
        gc.setStroke(Color.GOLD);
        gc.setLineWidth(4);
        gc.strokeRoundRect(x, y, boxW, boxH, 20, 20);

        // 3. Nagłówek
        gc.setFill(Color.GOLD);
        gc.setFont(Font.font("Verdana", FontWeight.BOLD, 28));
        gc.fillText("EKWIPUNEK", width / 2 - 80, y + 50);

        // 4. Instrukcja
        gc.setFill(Color.LIGHTGRAY);
        gc.setFont(Font.font("Arial", 14));
        gc.fillText("[E] Zamknij", width / 2 - 40, y + boxH - 20);

        drawItems(gc, player, x, y + 80);
    }

    private void drawItems(GraphicsContext gc, Player player, double startX, double startY) {
        Map<String, Integer> items = player.getInventory();
        int index = 0;
        int slotSize = 64;
        int gap = 20;
        int cols = 5;

        if (items.isEmpty()) {
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font(18));
            gc.fillText("Twój plecak jest pusty.", startX + 200, startY + 100);
            return;
        }

        for (Map.Entry<String, Integer> entry : items.entrySet()) {
            String name = entry.getKey();
            int count = entry.getValue();

            double slotX = startX + 50 + (index % cols) * (slotSize + gap);
            double slotY = startY + 20 + (index / cols) * (slotSize + gap);

            // Tło slotu
            gc.setFill(Color.rgb(100, 70, 50));
            gc.fillRect(slotX, slotY, slotSize, slotSize);
            gc.setStroke(Color.rgb(80, 50, 30));
            gc.strokeRect(slotX, slotY, slotSize, slotSize);

            // --- RYSOWANIE IKONY PRZEDMIOTU ---
            Image imgToDraw = null;
            switch (name) {
                case "Mikstura": imgToDraw = potionImg; break;
                case "Miecz":    imgToDraw = swordImg; break;
                case "Zbroja":   imgToDraw = armorImg; break;
                case "Różdżka":  imgToDraw = wandImg; break;
                //case "Pióro":    imgToDraw = featherImg; break;
            }

            if (imgToDraw != null) {

                gc.drawImage(imgToDraw, slotX + 8, slotY + 8, 48, 48);
            } else {

                switch (name) {
                    case "Mikstura" -> gc.setFill(Color.RED);
                    case "Miecz" -> gc.setFill(Color.SILVER);
                    case "Zbroja" -> gc.setFill(Color.GRAY);
                    case "Różdżka" -> gc.setFill(Color.MAGENTA);
                    case "Pióro" -> gc.setFill(Color.WHITE);
                    default -> gc.setFill(Color.YELLOW);
                }
                gc.fillRect(slotX + 10, slotY + 10, slotSize - 20, slotSize - 20);
            }

            // Licznik
            if (count > 1) {
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                gc.fillText(String.valueOf(count), slotX + slotSize - 15, slotY + slotSize - 5);
            }

            // Podpis
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", 10)); // Mniejsza czcionka
            gc.fillText(name, slotX, slotY + slotSize + 12);

            index++;
        }
    }
}