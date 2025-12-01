package pl.ortohero.app;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Player {

    private double x = 100;
    private double y = 100;
    private final double speed = 3;

    private final Image up1, down1, left1, right1;
    private String direction = "down";


    public double getX() { return x; }
    public double getY() { return y; }

    public double getSize() {
        return 32 * 3;
    }


    public Player() {
        up1 = new Image(getClass().getResourceAsStream("/images/boy_up_1.png"));
        down1 = new Image(getClass().getResourceAsStream("/images/boy_down_1.png"));
        left1 = new Image(getClass().getResourceAsStream("/images/boy_left_1.png"));
        right1 = new Image(getClass().getResourceAsStream("/images/boy_right_1.png"));
    }

    public void update(boolean up, boolean down, boolean left, boolean right) {
        if (up) {
            y -= speed;
            direction = "up";
        } else if (down) {
            y += speed;
            direction = "down";
        } else if (left) {
            x -= speed;
            direction = "left";
        } else if (right) {
            x += speed;
            direction = "right";
        }
    }

    public void render(GraphicsContext gc) {
        Image imageToDraw = switch (direction) {
            case "up" -> up1;
            case "left" -> left1;
            case "right" -> right1;
            default -> down1;
        };

        double scale = 3.0; // 3x większy
        double width = imageToDraw.getWidth() * scale;
        double height = imageToDraw.getHeight() * scale;

        gc.drawImage(imageToDraw, x, y, width, height);
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getHitboxX() { return x; }
    public double getHitboxY() { return y; }
    public double getHitboxSize() { return 32 * 3; }

}

