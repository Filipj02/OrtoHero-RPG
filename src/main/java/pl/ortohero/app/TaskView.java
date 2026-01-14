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

    public void render(GraphicsContext gc, double width, double height, Word currentWord, String message, Player player, boolean showRule) {
        double marginX = 100;
        double marginY = 100;

        // Tło okna
        gc.setFill(Color.rgb(40, 40, 40, 0.95));
        gc.fillRoundRect(marginX, marginY, width - 2 * marginX, height - 2 * marginY, 30, 30);
        gc.setStroke(Color.GOLD);
        gc.setLineWidth(3);
        gc.strokeRoundRect(marginX, marginY, width - 2 * marginX, height - 2 * marginY, 30, 30);

        if (currentWord == null) return;

        // TEKSTY
        gc.setTextAlign(TextAlignment.CENTER);

        // Nagłówek
        gc.setFont(Font.font("OpenDyslexic", FontWeight.BOLD, 24)); // lub Arial jeśli nie wgrałeś czcionki
        gc.setFill(Color.GOLD);
        gc.fillText("ZADANIE ORTOGRAFICZNE", width / 2, marginY + 50);

        // Słowo z maską
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("OpenDyslexic", FontWeight.BOLD, 42));
        gc.fillText(currentWord.getMaskedWord(), width / 2, height / 2 - 20);

        // KOMUNIKATY
        gc.setFont(Font.font("OpenDyslexic", 18));

        if (message != null && !message.isEmpty()) {
            if (message.startsWith("BRAWO") || message.startsWith("ZADANIE") || message.startsWith("BOSS") || message.startsWith("Dobrze")) {
                gc.setFill(Color.LIGHTGREEN);
            } else if (message.startsWith("BŁĄD") || message.startsWith("GAME")) {
                gc.setFill(Color.SALMON);
            } else {
                gc.setFill(Color.LIGHTGRAY);
            }
            gc.fillText(message, width / 2, height / 2 + 50);
        }

        // --- WYŚWIETLANIE DEFINICJI (RULE) PRZY BŁĘDZIE ---
        if (showRule) {
            gc.setFill(Color.YELLOW);
            gc.setFont(Font.font("OpenDyslexic", FontWeight.NORMAL, 16));

            // Pobieramy zasadę z obiektu Word
            String ruleText = "Zasada: " + currentWord.getRule();

            // Wyświetlamy ją pod komunikatem błędu (przesunięcie o +80 w dół)
            gc.fillText(ruleText, width / 2, height / 2 + 80);

            gc.setFill(Color.LIGHTGRAY);
            gc.setFont(Font.font(12));
            gc.fillText("(Naciśnij SPACJĘ, aby wylosować nowe słowo)", width / 2, height / 2 + 100);
        }
        // --------------------------------------------------

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