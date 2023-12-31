package entities;

import audio.AudioPlayer;
import gamestates.Playing;
import main.Game;
import utils.LoadSave;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;

import static utils.Constants.GRAVITY;
import static utils.Constants.ANIMATION_SPEED;
import static utils.Constants.PlayerConstants.*;

public class Player extends Entity {

    private BufferedImage[][] animations;
    private int[][] lvlData;
    private boolean moving = false, attacking = false;
    private boolean left, right;
    private float xDrawOffset = 21 * Game.SCALE;
    private float yDrawOffset = 4 * Game.SCALE;

    // Jumping / Gravity
    private boolean jump;
    private float jumpSpeed = -2.25f * Game.SCALE;
    private float fallSpeedAfterCollision = 0.5f * Game.SCALE;

    // StatusBarUI
    private BufferedImage statusBarImg;
    private int statusBarWidth = (int) (192 * Game.SCALE);
    private int statusBarHeight = (int) (58 * Game.SCALE);
    private int statusBarX = (int) (10 * Game.SCALE);
    private int statusBarY = (int) (10 * Game.SCALE);

    private int healthBarWidth = (int) (150 * Game.SCALE);
    private int healthBarHeight = (int) (4 * Game.SCALE);
    private int healthBarXStart = (int) (34 * Game.SCALE);
    private int healthBarYStart = (int) (14 * Game.SCALE);
    private int healthWidth = healthBarWidth;

    private int powerBarWidth = (int) (104 * Game.SCALE);
    private int powerBarHeight = (int) (2 * Game.SCALE);
    private int powerBarXStart = (int) (44 * Game.SCALE);
    private int powerBarYStart = (int) (34 * Game.SCALE);
    private int powerWidth = powerBarWidth;
    private int powerMaxValue = 200;
    private int powerValue = powerMaxValue;

    private int flipX = 0;
    private int flipW = 1;

    private boolean attackChecked;
    private Playing playing;

    private int tileY = 0;

    private boolean powerAttackActive;
    private int powerAttackTick, powerGrowTick, powerGrowSpeed = 15;

    public Player(float x, float y, int width, int height, Playing playing) {
        super(x, y, width, height);
        this.playing = playing;
        this.state = IDLE;
        this.maxHealth = 100;
        this.currentHealth = maxHealth;
        this.walkSpeed = Game.SCALE * 1.0f;
        loadAnimations();
        initHitbox(20, 27);
        initAttackBox();
    }

    public void setSpawn(Point spawn) {
        this.x = spawn.x;
        this.y = spawn.y;
        hitbox.x = x;
        hitbox.y = y;
    }

    private void initAttackBox() {
        attackBox = new Rectangle2D.Float(x, y, (int) (20 * Game.SCALE), (int) (20 * Game.SCALE));
        resetAttackBox();
    }

    public void update() {
        updateHealthBar();
        updatePowerBar();
        if (currentHealth <= 0) {
            if (state != DEAD) {
                state = DEAD;
                animationTick = 0;
                animationIndex = 0;
                playing.setPlayerDying(true);
                playing.getGame().getAudioPlayer().playEffect(AudioPlayer.DIE);
            } else if (animationIndex == GetSpriteAmount(DEAD) - 1 && animationTick >= ANIMATION_SPEED - 1) {
                playing.setGameOver(true);
                playing.getGame().getAudioPlayer().stopSong();
                playing.getGame().getAudioPlayer().playEffect(AudioPlayer.GAMEOVER);
            } else {
                updateAnimationTick();
            }
            return;
        }

        updateAttackBox();
        updatePos();
        if (moving) {
            checkPotionTouched();
            checkSpikesTouched();
            tileY = (int) (hitbox.y / Game.TILES_SIZE);
            if (powerAttackActive) {
                powerAttackTick++;
                if (powerAttackTick >= 35) {
                    powerAttackTick = 0;
                    powerAttackActive = false;
                }
            }
        }

        if (attacking || powerAttackActive)
            checkAttack();

        updateAnimationTick();
        setAnimation();
    }

    private void checkSpikesTouched() {
        playing.checkSpikesTouched(this);
    }

    private void checkPotionTouched() {
        playing.checkPotionTouched(hitbox);
    }

    private void checkAttack() {
        if (attackChecked || animationIndex != 1)
            return;
        attackChecked = true;

        if (powerAttackActive) attackChecked = false;
        playing.checkEnemyHit(attackBox);
        playing.checkObjectHit(attackBox);
        playing.getGame().getAudioPlayer().playAttackSound();
    }

    private void updateAttackBox() {
        if (right && left) {
            if (flipW == 1) attackBox.x = hitbox.x + hitbox.width + (int) (Game.SCALE * 10);
            else attackBox.x = hitbox.x - hitbox.width - (int) (Game.SCALE * 10);
        } else if (right || (powerAttackActive && flipW == 1)) {
            attackBox.x = hitbox.x + hitbox.width + (int) (Game.SCALE * 10);
        } else if (left || (powerAttackActive && flipW == -1)) {
            attackBox.x = hitbox.x - hitbox.width - (int) (Game.SCALE * 10);
        }
        attackBox.y = hitbox.y + (Game.SCALE * 10);
    }

    private void updateHealthBar() {
        healthWidth = (int) ((currentHealth / (float) maxHealth) * healthBarWidth);
    }

    private void updatePowerBar() {
        powerWidth = (int) ((powerValue / (float) powerMaxValue) * powerBarWidth);

        powerGrowTick++;
        if (powerGrowTick >= powerGrowSpeed) {
            powerGrowTick = 0;
            changePower(1);
        }
    }

    public void render(Graphics g, int lvlOffset) {
        g.drawImage(animations[state][animationIndex],
                (int) (hitbox.x - xDrawOffset) - lvlOffset + flipX,
                (int) (hitbox.y - yDrawOffset),
                width * flipW, height, null);
//      drawAttackBox(g, lvlOffset);
        drawUI(g);
    }

    private void drawUI(Graphics g) {
        //background ui
        g.drawImage(statusBarImg, statusBarX, statusBarY, statusBarWidth, statusBarHeight, null);

        //health bar
        g.setColor(Color.red);
        g.fillRect(healthBarXStart + statusBarX, healthBarYStart + statusBarY, healthWidth, healthBarHeight);

        //power bar
        g.setColor(Color.yellow);
        g.fillRect(powerBarXStart + statusBarX, powerBarYStart + statusBarY, powerWidth, powerBarHeight);
    }

    public void changeHealth(int value) {
        currentHealth += value;
        if (currentHealth <= 0)
            currentHealth = 0;
        else if (currentHealth >= maxHealth)
            currentHealth = maxHealth;
    }

    public void changePower(int value) {
        powerValue += value;
        if (powerValue >= powerMaxValue)
            powerValue = powerMaxValue;
        else if (powerValue <= 0)
            powerValue = 0;
    }

    public void kill() {
        currentHealth = 0;
    }

    private void loadAnimations() {

        BufferedImage img = LoadSave.GetSpriteAtlas(LoadSave.PLAYER_ATLAS);

        animations = new BufferedImage[7][8];
        for (int j = 0; j < animations.length; j++) {
            for (int i = 0; i < animations[j].length; i++) {
                animations[j][i] = img.getSubimage(i * 64, j * 40, 64, 40);
            }
        }

        statusBarImg = LoadSave.GetSpriteAtlas(LoadSave.STATUS_BAR);
    }

    private void updateAnimationTick() {
        animationTick++;
        if (animationTick >= ANIMATION_SPEED) {
            animationTick = 0;
            animationIndex++;
            if (animationIndex >= GetSpriteAmount(state)) {
                animationIndex = 0;
                attacking = false;
                attackChecked = false;
            }
        }
    }

    private void setAnimation() {
        int startAnimation = state;

        state = moving ? RUNNING : IDLE;

        if (inAir) {
            state = (airSpeed < 0) ? JUMP : FALLING;
        }

        if (powerAttackActive) {
            state = ATTACK;
            animationIndex = 1;
            animationTick = 0;
            return;
        }

        if (attacking) {
            state = ATTACK;
            if (startAnimation != ATTACK) {
                animationIndex = 1;
                animationTick = 0;
                return;
            }
        }

        if (startAnimation != state)
            resetAnimationTick();
    }

    private void resetAnimationTick() {
        animationTick = 0;
        animationIndex = 0;
    }

    private void updatePos() {
        moving = false;

        if (jump) jump();
        if (!inAir)
            if (!powerAttackActive)
                if ((!left && !right) || (right && left)) return;

        float xSpeed = 0;

        if (left && !right) {
            xSpeed -= walkSpeed;
            flipX = width;
            flipW = -1;
        }
        if (right && !left) {
            xSpeed += walkSpeed;
            flipX = 0;
            flipW = 1;
        }


        if (powerAttackActive) {
            if ((!left && !right) || (left && right)) {
                if (flipW == -1) xSpeed = -walkSpeed;
                else xSpeed = walkSpeed;
            }

            xSpeed *= 3;
        }

        if (!inAir)
            if (!isEntityOnFloor(hitbox, lvlData)) inAir = true;

        if (inAir && !powerAttackActive) {
            if (canMoveHere(hitbox.x, hitbox.y + airSpeed, hitbox.width, hitbox.height, lvlData)) {
                hitbox.y += airSpeed;
                airSpeed += GRAVITY;
            } else {
                hitbox.y = getEntityYPosUnderRoofOrAboveFloor(hitbox, airSpeed);
                if (airSpeed > 0) {
                    inAir = false;
                    airSpeed = 0;
                } else {
                    airSpeed = fallSpeedAfterCollision;
                }
            }
        }

        updateXPos(xSpeed);
        moving = true;
    }


    private void jump() {
        if (inAir) return;
        playing.getGame().getAudioPlayer().playEffect(AudioPlayer.JUMP);
        inAir = true;
        airSpeed = jumpSpeed;
    }

    private void updateXPos(float xSpeed) {
        if (canMoveHere(hitbox.x + xSpeed, hitbox.y, hitbox.width, hitbox.height, lvlData)) {
            hitbox.x += xSpeed;
        } else {
            hitbox.x = getEntityXPosNextToWall(hitbox, xSpeed);
            if (powerAttackActive) {
                powerAttackActive = false;
                powerAttackTick = 0;
            }
        }
    }

    public boolean canCannonSeePlayer(int[][] lvlData, Rectangle2D.Float cannonHitbox, int yTile) {
        int firstXTile = (int) (hitbox.x / Game.TILES_SIZE);
        int secondXTile = (int) (cannonHitbox.x / Game.TILES_SIZE);

        if (firstXTile > secondXTile)
            return isAllTilesClear(secondXTile, firstXTile, yTile, lvlData);
        else
            return isAllTilesClear(firstXTile, secondXTile, yTile, lvlData);
    }

    public void loadLvlData(int[][] lvlData) {
        this.lvlData = lvlData;
        if (!isEntityOnFloor(hitbox, lvlData)) inAir = true;
    }

    public void resetDirectionBooleans() {
        left = false;
        right = false;
    }

    public void setLeft(boolean left) {
        this.left = left;
    }


    public void setRight(boolean right) {
        this.right = right;
    }


    public void setAttacking(boolean attacking) {
        this.attacking = attacking;
    }

    public void setJump(boolean jump) {
        this.jump = jump;
    }

    public void resetAll() {
        resetDirectionBooleans();
        inAir = false;
        attacking = false;
        moving = false;
        airSpeed = 0f;
        state = IDLE;
        currentHealth = maxHealth;

        hitbox.x = x;
        hitbox.y = y;
        resetAttackBox();


        if (!isEntityOnFloor(hitbox, lvlData))
            inAir = true;
    }

    private void resetAttackBox() {
        if (flipW == 1) attackBox.x = hitbox.x + hitbox.width + (int) (Game.SCALE * 10);
        else attackBox.x = hitbox.x - hitbox.width - (int) (Game.SCALE * 10);

    }

    public int getTileY() {
        return tileY;
    }

    public void powerAttack() {
        if (powerAttackActive) return;
        if (powerValue >= 60) {
            powerAttackActive = true;
            changePower(-60);
        }
    }
}
