package objects;

import java.awt.geom.Rectangle2D;

import main.Game;

import static utils.Constants.Projectiles.*;

public class Projectile {
	private Rectangle2D.Float hitbox;
	private int direction;
	private boolean active = true;

	public Projectile(int x, int y, int direction) {
		int xOffset = (int) (-3 * Game.SCALE);
		int yOffset = (int) (5 * Game.SCALE);

		if (direction == 1)
			xOffset = (int) (29 * Game.SCALE);

		hitbox = new Rectangle2D.Float(x + xOffset, y + yOffset, CANNON_BALL_WIDTH, CANNON_BALL_HEIGHT);
		this.direction = direction;
	}

	public void updatePos() {
		hitbox.x += direction * SPEED;
	}

	public void setPos(int x, int y) {
		hitbox.x = x;
		hitbox.y = y;
	}

	public Rectangle2D.Float getHitbox() {
		return hitbox;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isActive() {
		return active;
	}

	public boolean isProjectileHittingLevel(int[][] lvlData) {
		return isSolid(hitbox.x + hitbox.width / 2, hitbox.y + hitbox.height / 2, lvlData);
	}

	//breaking DRY rule by copying code below, but it might be correct to do here
	private boolean isSolid(float x, float y, int[][] lvlData) {
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
}
