package entities;

import main.Game;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public abstract class Entity {

    protected float x, y;
    protected int width, height;
    protected Rectangle2D.Float hitbox;

    public Entity(float x, float y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Rectangle2D.Float getHitbox() {
        return hitbox;
    }

    protected void drawHitbox(Graphics g, int xLevelOffset) {
        //for debugging the hitbox
        g.setColor(Color.PINK);
        g.drawRect((int) hitbox.x - xLevelOffset, (int) hitbox.y, (int) hitbox.width, (int) hitbox.height);
    }

    protected void initHitbox(float x, float y, int width, int height) {
        hitbox = new Rectangle2D.Float(x, y, width, height);
    }

    protected boolean canMoveHere(float x, float y, float width, float height, int[][] lvlData) {
        if (!isSolid(x, y, lvlData))
            if (!isSolid(x + width, y + height, lvlData))
                if (!isSolid(x + width, y, lvlData))
                    if (!isSolid(x, y + height, lvlData))
                        return true;
        return false;
    }

    protected boolean isSolid(float x, float y, int[][] lvlData) {
        int maxWidth = lvlData[0].length * Game.TILES_SIZE;
        if (x < 0 || x >= maxWidth)
            return true;
        if (y < 0 || y >= Game.GAME_HEIGHT)
            return true;

        float xIndex = x / Game.TILES_SIZE;
        float yIndex = y / Game.TILES_SIZE;

        int value = lvlData[(int) yIndex][(int) xIndex];

        if (value >= 48 || value < 0 || value != 11)
            return true;
        return false;
    }

    protected float getEntityXPosNextToWall(Rectangle2D.Float hitbox, float xSpeed) {
        int currentTile = (int) (hitbox.x / Game.TILES_SIZE);
        if (xSpeed > 0) {
            //Right
            int tileXPos = currentTile * Game.TILES_SIZE;
            int xOffset = (int) (Game.TILES_SIZE - hitbox.width);

            return tileXPos + xOffset - 1;
        } else {
            //Left

            return currentTile * Game.TILES_SIZE;
        }
    }

    protected float getEntityYPosUnderRoofOrAboveFloor(Rectangle2D.Float hitbox, float airSpeed) {
        int currentTile = (int) (hitbox.y / Game.TILES_SIZE);
        if (airSpeed > 0) {
            //Falling
            int tileYPos = currentTile * Game.TILES_SIZE;
            int yOffset = (int) (Game.TILES_SIZE - hitbox.height);

            return tileYPos + yOffset - 1;
        } else {
            //Jumping

            return currentTile * Game.TILES_SIZE;
        }
    }

    protected boolean isEntityOnFloor(Rectangle2D.Float hitbox, int[][] lvlData) {
        //Check the pixel between bottom corners
        if (!isSolid(hitbox.x, hitbox.y + hitbox.height + 1, lvlData))
            if (!isSolid(hitbox.x + width, hitbox.y + hitbox.height + 1, lvlData))
                return false;

        return true;
    }

    /**
     * We just check the bottomleft of the enemy here +/- the xSpeed. We never check bottom right in case the
     * enemy is going to the right. It would be more correct checking the bottomleft for left direction and
     * bottomright for the right direction. But it wont have big effect in the game. The enemy will simply change
     * direction sooner when there is an edge on the right side of the enemy, when its going right.
     */
    public boolean isFloor(Rectangle2D.Float hitbox, float xSpeed, int[][] lvlData) {
        return isSolid(hitbox.x + xSpeed, hitbox.y + hitbox.height + 1, lvlData);
    }

}
