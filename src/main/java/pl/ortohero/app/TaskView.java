package pl.ortohero.app;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class TaskView {

    // Kolory i fonty
    private static final Color BG_COLOR = Color.rgb(40, 40, 60, 0.95); // Ciemnogranatowy, prawie nieprzezroczysty
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color ACCENT_COLOR = Color.GOLD;

    public void render(GraphicsContext gc, double width, double height, Word currentWord, String message) {
        // 1. TŁO (Ciemna ramka na środku ekranu)
        double marginX = 200;
        double marginY = 150;
        double boxWidth = width - (2 * marginX);
        double boxHeight = height - (2 * marginY);

        gc.setFill(BG_COLOR);
        gc.fillRoundRect(marginX, marginY, boxWidth, boxHeight, 30, 30);

        // Obramowanie
        gc.setStroke(ACCENT_COLOR);
        gc.setLineWidth(3);
        gc.strokeRoundRect(marginX, marginY, boxWidth, boxHeight, 30, 30);

        // Jeśli nie ma słowa, nie rysuj reszty
        if (currentWord == null) return;

        // 2. TYTUŁ
        gc.setFill(ACCENT_COLOR);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Verdana", FontWeight.BOLD, 28));
        gc.fillText("ZADANIE ORTOGRAFICZNE", width / 2, marginY + 60);

        // 3. SŁOWO Z LUKĄ (Najważniejsza część)
        gc.setFill(TEXT_COLOR);
        gc.setFont(Font.font("Courier New", FontWeight.BOLD, 48)); // Courier jest stałej szerokości - dobry do luk
        gc.fillText(currentWord.getMaskedWord(), width / 2, height / 2);

        // 4. INSTRUKCJA / KOMUNIKAT
        gc.setFont(Font.font("Arial", 20));
        gc.setFill(Color.LIGHTGRAY);
        if (message != null && !message.isEmpty()) {
            gc.fillText(message, width / 2, height / 2 + 80);
        } else {
            gc.fillText("Wpisz brakującą literę na klawiaturze...", width / 2, height / 2 + 80);
        }

        // 5. Instrukcja wyjścia (tymczasowa)
        gc.setFont(Font.font("Arial", 14));
        gc.fillText("[ESC] Anuluj / Wyjdź", width / 2, marginY + boxHeight - 20);
    }
}