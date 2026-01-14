package pl.ortohero.app;

import javafx.scene.control.Slider;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class SoundManager {

    private MediaPlayer musicPlayer;

    // Efekty dźwiękowe (AudioClip jest lepszy do krótkich dźwięków niż MediaPlayer)
    private AudioClip hitSound;
    private AudioClip gameOverSound;
    private AudioClip winSound;

    public void initSounds() {
        try {
            // 1. Muzyka w tle
            String musicPath = getResourcePath("music.mp3");
            if (musicPath != null) {
                Media sound = new Media(musicPath);
                musicPlayer = new MediaPlayer(sound);
                musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                musicPlayer.setVolume(0.5);
                musicPlayer.play();
            }

            // 2. Efekty dźwiękowe (bezpieczne ładowanie)
            String hitPath = getResourcePath("hit.mp3");
            if (hitPath != null) hitSound = new AudioClip(hitPath);

            String overPath = getResourcePath("gameover.mp3");
            if (overPath != null) gameOverSound = new AudioClip(overPath);

            String winPath = getResourcePath("win.mp3");
            if (winPath != null) winSound = new AudioClip(winPath);

        } catch (Exception e) {
            System.err.println("Ogólny błąd dźwięku: " + e.getMessage());
        }
    }

    // Metoda pomocnicza - sprawdza czy plik istnieje ZANIM spróbuje go użyć
    private String getResourcePath(String filename) {
        var url = getClass().getResource("/sound/" + filename);
        if (url == null) {
            System.err.println("⚠️ OSTRZEŻENIE: Nie znaleziono pliku: " + filename + " (Sprawdź folder resources/sounds/)");
            return null;
        }
        return url.toExternalForm();
    }

    // Metody odtwarzania
    public void playHit() {
        if (hitSound != null) hitSound.play();
    }

    public void playGameOver() {
        if (musicPlayer != null) musicPlayer.stop(); // Zatrzymujemy muzykę tła
        if (gameOverSound != null) gameOverSound.play();
    }

    public void playWin() {
        if (musicPlayer != null) musicPlayer.stop(); // Zatrzymujemy muzykę tła
        if (winSound != null) winSound.play();
    }

    // Wznawianie muzyki po restarcie
    public void restartMusic() {
        if (musicPlayer != null) {
            musicPlayer.stop();
            musicPlayer.play();
        }
    }

    public void bindVolumeSlider(Slider slider) {
        if (musicPlayer != null) {
            slider.setValue(musicPlayer.getVolume());
            slider.valueProperty().addListener((obs, oldVal, newVal) -> {
                musicPlayer.setVolume(newVal.doubleValue());
            });
        }
    }
}