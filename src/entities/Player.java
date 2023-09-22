package entities;

import main.Game;
import utils.LoadSave;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import static utils.Constants.PlayerConstants.*;

public class Player extends Entity {

    private BufferedImage[][] animations;
    private int animationTick, animationIndex, animationSpeed = 25;
    private int playerAction = IDLE;
    private boolean moving = false, attacking = false;
    private boolean left, up, right, down;
    private float playerSpeed = 2.0f;

    private int[][] lvlData;
    private float xDrawOffset = 21 * Game.SCALE;
    private float yDrawOffset = 2 * Game.SCALE;

    public Player(float x, float y, int width, int height) {
        super(x, y, width, height);
        loadAnimations();
        initHitbox(x, y, 20 * Game.SCALE, 28 * Game.SCALE);
    }

    public void update() {
        updatePos();
        updateAnimationTick();
        setAnimation();
    }

    public void render(Graphics g) {
        g.drawImage(animations[playerAction][animationIndex], (int) (hitbox.x - xDrawOffset), (int) (hitbox.y - yDrawOffset), width, height, null);
//        drawHitbox(g);
    }

    private void loadAnimations() {

            BufferedImage img = LoadSave.GetSpriteAtlas(LoadSave.PLAYER_ATLAS);

            animations = new BufferedImage[9][6];
            for (int j = 0; j < animations.length; j++) {
                for (int i = 0; i < animations[j].length; i++) {
                    animations[j][i] = img.getSubimage(i * 64, j * 40, 64, 40);
                }
            }

    }

    private void updateAnimationTick() {
        animationTick++;
        if (animationTick >= animationSpeed) {
            animationTick = 0;
            animationIndex++;
            if (animationIndex >= GetSpriteAmount(playerAction)) {
                animationIndex = 0;
                attacking = false;
            }
        }
    }

    private void setAnimation() {
        int startAnimation = playerAction;

        if (moving)
            playerAction = RUNNING;
        else
            playerAction = IDLE;

        if (attacking)
            playerAction = ATTACK_1;

        if (startAnimation != playerAction)
            resetAnimationTick();
    }

    private void resetAnimationTick() {
        animationTick = 0;
        animationIndex = 0;
    }

    private void updatePos() {
        moving = false;

        if (!left && !right && !up && !down)
            return;

        float xSpeed = 0, ySpeed = 0;

        if (left && !right)
            xSpeed = -playerSpeed;
        else if (right && !left)
            xSpeed = playerSpeed;

        if (up && !down)
            ySpeed = -playerSpeed;
        else if (down && !up)
            ySpeed = playerSpeed;

        if (canMoveHere(hitbox.x + xSpeed, hitbox.y + ySpeed, hitbox.width, hitbox.height, lvlData)) {
            hitbox.x += xSpeed;
            hitbox.y += ySpeed;
            moving = true;
        }

    }

    public void loadLvlData(int[][] lvlData) {
        this.lvlData = lvlData;
    }

    public void resetDirectionBooleans() {
        left = false;
        right = false;
        up = false;
        down = false;
    }

    public void setLeft(boolean left) {
        this.left = left;
    }

    public void setUp(boolean up) {
        this.up = up;
    }

    public void setRight(boolean right) {
        this.right = right;
    }

    public void setDown(boolean down) {
        this.down = down;
    }

    public void setAttacking(boolean attacking) {
        this.attacking = attacking;
    }
}
