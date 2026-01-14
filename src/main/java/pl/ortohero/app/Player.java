package pl.ortohero.app;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import java.util.HashMap;
import java.util.Map;

public class Player {

    private double x, y;
    private int lives;
    private int level;
    private int wordsSolvedInCurrentLevel = 0;
    private Map<String, Integer> inventory;
    private double speed = 2.0;


    private String direction = "down";
    private int spriteCounter = 0;
    private int spriteNum = 1;


    private transient Image up1, up2, down1, down2, left1, left2, right1, right2;

    public Player() {
        this.lives = 3;
        this.level = 1;
        this.inventory = new HashMap<>();
        loadImages();
    }

    private void loadImages() {
        try {

            up1 = new Image(getClass().getResourceAsStream("/images/boy_up_1.png"));
            up2 = new Image(getClass().getResourceAsStream("/images/boy_up_2.png"));
            down1 = new Image(getClass().getResourceAsStream("/images/boy_down_1.png"));
            down2 = new Image(getClass().getResourceAsStream("/images/boy_down_2.png"));
            left1 = new Image(getClass().getResourceAsStream("/images/boy_left_1.png"));
            left2 = new Image(getClass().getResourceAsStream("/images/boy_left_2.png"));
            right1 = new Image(getClass().getResourceAsStream("/images/boy_right_1.png"));
            right2 = new Image(getClass().getResourceAsStream("/images/boy_right_2.png"));
        } catch (Exception e) {
            System.err.println("Błąd ładowania grafik gracza: " + e.getMessage());
        }
    }

    public void update(boolean up, boolean down, boolean left, boolean right) {
        boolean isMoving = false;

        if (up) {
            y -= speed;
            direction = "up";
            isMoving = true;
        }
        if (down) {
            y += speed;
            direction = "down";
            isMoving = true;
        }
        if (left) {
            x -= speed;
            direction = "left";
            isMoving = true;
        }
        if (right) {
            x += speed;
            direction = "right";
            isMoving = true;
        }

        // --- LOGIKA ANIMACJI ---
        if (isMoving) {
            spriteCounter++;

            if (spriteCounter > 10) {
                if (spriteNum == 1) {
                    spriteNum = 2;
                } else if (spriteNum == 2) {
                    spriteNum = 1;
                }
                spriteCounter = 0;
            }
        } else {

            spriteCounter = 0;
            spriteNum = 1;
        }
    }

    public void render(GraphicsContext gc) {
        Image imageToDraw = null;

        switch (direction) {
            case "up":
                if (spriteNum == 1) imageToDraw = up1; else imageToDraw = up2;
                break;
            case "down":
                if (spriteNum == 1) imageToDraw = down1; else imageToDraw = down2;
                break;
            case "left":
                if (spriteNum == 1) imageToDraw = left1; else imageToDraw = left2;
                break;
            case "right":
                if (spriteNum == 1) imageToDraw = right1; else imageToDraw = right2;
                break;
        }


        if (imageToDraw != null) {

            gc.drawImage(imageToDraw, x , y , 32, 32);
        } else {
            gc.setFill(Color.BLUE);
            gc.fillRect(x, y, 32, 32);
        }
    }


    public void resetAnimationState() {
        spriteNum = 1;
        spriteCounter = 0;
    }


    public void addSuccess() {
        wordsSolvedInCurrentLevel++;
        int wordsNeeded = 5 + level;
        if (wordsSolvedInCurrentLevel >= wordsNeeded) {
            levelUp();
        }
    }

    private void levelUp() {
        if (level >= 10) return;
        level++;
        wordsSolvedInCurrentLevel = 0;
        if (lives < 3) lives++;
        System.out.println("AWANS! Poziom: " + level);
    }

    public String getProgressString() {
        if (level >= 10) return "MAX";
        int wordsNeeded = 5 + level;
        return wordsSolvedInCurrentLevel + "/" + wordsNeeded;
    }

    private boolean armorEquipped = false; // Czy zbroja założona?


    public void equipArmor() {
        this.armorEquipped = true; // Włączamy ochronę
        // Ustawiamy życie na 4 (ponad limit)
        System.out.println("Zbroja założona! Życie");
    }


    public boolean hasArmor() {
        return armorEquipped;
    }
    public void breakArmor() {
        this.armorEquipped = false; // Wyłącza ochronę
        System.out.println("Zbroja pękła!");
    }

    public void setPosition(double x, double y) { this.x = x; this.y = y; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getHitboxX() { return x + 8; }
    public double getHitboxY() { return y + 8; }
    public double getHitboxSize() { return 16; }

    public int getLives() { return lives; }
    public void loseLife() { lives--; }
    public void heal() { if(lives < 3) lives = 3; }

    public void reset() {
        this.lives = 3;
        this.level = 1;
        this.wordsSolvedInCurrentLevel = 0;
        this.inventory.clear();
        this.direction = "down";
    }
    public int getLevel() { return level; }

    public void addItem(String item) { inventory.put(item, inventory.getOrDefault(item, 0) + 1); }
    public boolean hasItem(String item) { return inventory.getOrDefault(item, 0) > 0; }
    public void useItem(String item) {
        if (hasItem(item)) inventory.put(item, inventory.get(item) - 1);
        if (inventory.get(item) <= 0) {
            inventory.remove(item);
        }
    }
    public Map<String, Integer> getInventory() { return inventory; }

    public int getWordsSolvedInCurrentLevel() { return wordsSolvedInCurrentLevel; }
    public void setWordsSolvedInCurrentLevel(int w) { this.wordsSolvedInCurrentLevel = w; }

}