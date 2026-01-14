package pl.ortohero.app;

import java.util.ArrayList;
import java.util.List;

public class CombatSystem {

    private WordBank wordBank;
    private Word currentWord;
    private String message = "";
    private String typedAnswer = "";
    private int remainingWordsToSolve = 0;
    private boolean isTaskSolved = false;
    private boolean showingError = false;
    private GameObject activeObject;


    private SoundManager soundManager;


    public CombatSystem(SoundManager soundManager) {
        this.wordBank = new WordBank();
        this.soundManager = soundManager; // <--- TEJ LINII BRAKOWAŁO LUB BYŁA BŁĘDNA!
    }

    public void startCombat(GameObject obj, int playerLevel) {
        this.activeObject = obj;
        this.isTaskSolved = false;
        this.showingError = false;


        if (obj.getName().equals("BOSS")) {
            this.remainingWordsToSolve = 15;
        } else {
            this.remainingWordsToSolve = 5;
        }

        loadNextWord(playerLevel);
    }

    public void loadNextWord(int playerLevel) {
        List<Integer> diffs = new ArrayList<>();
        if (activeObject != null && activeObject.getName().equals("BOSS")) diffs.add(3);
        else {
            if (playerLevel < 3) diffs.add(1);
            else if (playerLevel < 6) { diffs.add(1); diffs.add(2); }
            else if (playerLevel < 9) diffs.add(2);
            else { diffs.add(2); diffs.add(3); }
        }

        List<Word> batch = wordBank.getWordsByDifficulty(1, diffs);
        if (!batch.isEmpty()) {
            currentWord = batch.get(0);
            message = "Seria: " + remainingWordsToSolve + " słów do końca.";
            typedAnswer = "";
        } else {
            currentWord = new Word("brak", "b", "Brak słów", 1);
        }
    }

    // Główna logika pisania - zwraca TRUE jeśli gra ma się zakończyć (wygrana/przegrana)
    public boolean handleTyping(String charInput, Player player) {
        if (charInput.trim().isEmpty()) return false;

        typedAnswer += charInput;
        String target = currentWord.getTarget();

        if (target.toLowerCase().startsWith(typedAnswer.toLowerCase())) {
            if (typedAnswer.equalsIgnoreCase(target)) {
                // SUKCES

                player.addSuccess();

                remainingWordsToSolve --;

                if (remainingWordsToSolve > 0) {
                    loadNextWord(player.getLevel());
                    message = "Dobrze! Zostało: " + remainingWordsToSolve;
                } else {
                    isTaskSolved = true;
                    message = (activeObject.getName().equals("BOSS")) ?
                            "BOSS POKONANY! GRATULACJE! [SPACJA]" : "ZADANIE WYKONANE! [SPACJA]";
                }
            } else {
                message = "Wpisano: " + typedAnswer + "...";
            }
        } else if (isOrthographicError(target, typedAnswer)) {
            // BŁĄD
            if (player.hasArmor()) {
                // Masz zbroję -> NIE tracisz życia
                soundManager.playHit();
                message = "ZBROJA OCHRONIŁA CIĘ! " + currentWord.getRule();
                showingError = true;
                player.breakArmor();

            } else {
                player.loseLife();
                if (player.getLives() <= 0) {
                    soundManager.playGameOver(); // Dźwięk przegranej
                    message = "GAME OVER! [SPACJA] restart.";
                    isTaskSolved = true;
                    return true;
                } else {
                    soundManager.playHit(); // Dźwięk utraty życia
                    showingError = true;
                    message = "BŁĄD ORTOGRAFICZNY! [SPACJA]";
                }
                typedAnswer = "";
                // ---------------------
            }
        }
        else {
            typedAnswer = typedAnswer.substring(0, typedAnswer.length() - 1);
        }
        return false;
    }

    private boolean isOrthographicError(String target, String input) {
        target = target.toLowerCase();
        input = input.toLowerCase();
        String lastChar = input.substring(input.length() - 1);
        if (target.startsWith("rz") && lastChar.equals("ż")) return true;
        if (target.startsWith("ż") && lastChar.equals("r")) return true;
        if (target.startsWith("ch") && lastChar.equals("h")) return true;
        if (target.startsWith("h") && lastChar.equals("c")) return true;
        if (target.startsWith("u") && lastChar.equals("ó")) return true;
        if (target.startsWith("ó") && lastChar.equals("u")) return true;
        return false;
    }
    public void useSwordEffect() {
        if (remainingWordsToSolve > 0) {
            remainingWordsToSolve--; // Kasujemy jedno słowo!
            message = "Użyto miecza! Zostało słów: " + remainingWordsToSolve;

            // Sprawdzamy czy to nie był koniec walki
            if (remainingWordsToSolve <= 0) {
                isTaskSolved = true;
                message = "POKONANY MIECZEM! [SPACJA]";
                if (activeObject != null && activeObject.getName().equals("BOSS")) {
                    soundManager.playWin();
                }
            }
        }
    }


    // Gettery i Settery dla MainApp
    public Word getCurrentWord() { return currentWord; }
    public String getMessage() { return message; }
    public boolean isTaskSolved() { return isTaskSolved; }
    public boolean isShowingError() { return showingError; }
    public void setShowingError(boolean b) { this.showingError = b; }
    public GameObject getActiveObject() { return activeObject; }
    public void setActiveObject(GameObject o) { this.activeObject = o; }
}