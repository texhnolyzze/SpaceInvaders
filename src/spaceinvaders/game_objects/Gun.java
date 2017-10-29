package spaceinvaders.game_objects;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import spaceinvaders.App;
import static spaceinvaders.App.PIXEL_SIZE;
import spaceinvaders.SoundStore;
import spaceinvaders.TickTimer;
import static spaceinvaders.game_objects.General.DIRECTION_LEFT;
import static spaceinvaders.game_objects.General.DIRECTION_NONE;
/**
 *
 * @author Texhnolyze
 */
public class Gun {
    
    public static long  MIN_DELAY_BETWEEN_SHOTS_MS = 600;
    
    private static final Image GUN = App.getImage("gun");
    
    private static final Image[] GUN_EXPLOSE = {
        App.getImage("gun_explose_0"),
        App.getImage("gun_explose_1")
    };
    
    private final int init_x;
    
    private int x;
    private final int y;
    
    private int dir = DIRECTION_NONE;
    private final int vel;
    
    private long lastShotTimeMs = -1;
    
    private boolean destroyed;
    
    public Gun(int x_pix, int y_pix, int vel_pix) {
        this.y = y_pix * PIXEL_SIZE;
        this.x = init_x = x_pix * PIXEL_SIZE;
        this.vel = vel_pix * PIXEL_SIZE;
    }
    
    public void reset() {
        x = init_x;
        destroyed = false;
        lastShotTimeMs = -1;
        dir = DIRECTION_NONE;
    }
    
    public void move() {
        if (dir != DIRECTION_NONE) {
            if (dir == DIRECTION_LEFT) {
                if (x > InvadersWave.WAVE_LEFT_BOUND) x -= vel;
            } else {
                if (x < InvadersWave.WAVE_RIGHT_BOUND - GUN.getWidth()) x += vel;
            }
        }
    }
    
    public void setDirection(int dir) {
        this.dir = dir;
    }
    
    private int destroyedFrameIdx;
    private TickTimer timer = new TickTimer();
    
    public void draw(GraphicsContext gc) {
        if (!destroyed) gc.drawImage(GUN, x, y);
        else {
            timer.tick();
            gc.drawImage(GUN_EXPLOSE[destroyedFrameIdx], x, y);
            if (timer.passed(5)) {
                timer.reset();
                destroyedFrameIdx = (destroyedFrameIdx + 1) % GUN_EXPLOSE.length;
            }
        }
    }
    
    public Shot bang() {
        if (System.currentTimeMillis() - lastShotTimeMs >= MIN_DELAY_BETWEEN_SHOTS_MS) {
            int w = (int) GUN.getWidth();
            int s_h = (int) Shot.getHeightOf(Shot.GUN_SHOT);
            int s_x = x + (w >> 1);
            int s_y = y - s_h;
            lastShotTimeMs = System.currentTimeMillis();
            SoundStore.GUN_SHOOT.play(false);
            return new Shot(s_x, s_y, 2.3F * PIXEL_SIZE, Shot.GUN_SHOT);
        } else return null;
    }
    
    public boolean isDestroyedBy(Shot s) {
        if (s.getType() != Shot.GUN_SHOT) {
            int s_x = s.getX();
            int s_y = s.getY();
            int s_w = s.getWidth();
            int s_h = s.getHeight();
            int w = (int) GUN.getWidth();
            int h = (int) GUN.getHeight();
            if (General.isRectanglesOverlap(x, s_x, y, s_y, w, s_w, h, s_h)) {
                s.hitted();
                SoundStore.GUN_EXPLOSE.play(true);
                dir = DIRECTION_NONE;
                return destroyed = true;
            } else return false;
        }
        return false;
    }
    
    public boolean isDestroyed() {
        return destroyed;
    }
    
}
