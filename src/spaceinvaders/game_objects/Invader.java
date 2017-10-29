package spaceinvaders.game_objects;

import java.util.Random;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import spaceinvaders.App;
import static spaceinvaders.App.PIXEL_SIZE;
import spaceinvaders.SoundStore;
import static spaceinvaders.game_objects.Shot.INVADERS_SHOT_1;
import static spaceinvaders.game_objects.Shot.INVADERS_SHOT_2;

/**
 *
 * @author Texhnolyze
 */
public class Invader {
    
    public static final int INVADER_0 = 0;
    public static final int INVADER_1 = 1;
    public static final int INVADER_2 = 2;

    private static final long VALID_TIME_AFTER_DEATH_MS = 300;
    
    private static final Image[][] INVADERS = {
        {
            App.getImage("invader_0_0"), 
            App.getImage("invader_0_1")
        },
        {
            App.getImage("invader_1_0"), 
            App.getImage("invader_1_1")
        },
        {
            App.getImage("invader_2_0"), 
            App.getImage("invader_2_1")
        },
    };
    
    private static final Image EXPLOSION = App.getImage("explosion");
    
    private int x;
    private int y;
    
    private int init_y;
    private final int init_x;
    
    private final int type;
    
    private boolean dead;
    private long deathTimeMs;
    
    public Invader(int type, int x, int y) {
        this.x = init_x = x;
        this.y = init_y = y;
        this.type = type;
    }
    
    public void reset(int dy) {
        x = init_x;
        y = init_y = init_y + dy;
        dead = false;
    }
    
    public int getType() {
        return type;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    int getWidth() {
        return getWidthOf(type);
    }
    
    int getHeight() {
        return getHeightOf(type);
    }

    void move(int vel_x, int vel_y) {
        if (!dead) {
            x += vel_x;
            y += vel_y;
        }
    }
    
    public void draw(GraphicsContext gc, int idx) {
        if (!dead) gc.drawImage(INVADERS[type][idx], x, y);
        else if (isValid()) gc.drawImage(EXPLOSION, x, y);
    }

    private static final Random RANDOM = new Random(); 
    
    Shot bang(int waveSize) {
        if (RANDOM.nextInt(650) == 1) {
            float vel = (float) (1.8F + Math.random()) * PIXEL_SIZE;
            return new Shot(
                    x + getWidth() / 2, y + getHeight(), vel, 
                    RANDOM.nextBoolean() ? INVADERS_SHOT_1 : INVADERS_SHOT_2
            );
        }
        return null;
    }
    
    void handle(Shot s) {
        int s_x = s.getX();
        int s_y = s.getY();
        int s_w = s.getWidth();
        int s_h = s.getHeight();
        int w = getWidth();
        int h = getHeight();
        if (General.isRectanglesOverlap(x, s_x, y, s_y, w, s_w, h, s_h)) {
            dead = true;
            deathTimeMs = System.currentTimeMillis();
            SoundStore.INVADER_KILLED.play(false);
            s.hitted();
        }
    }
    
    boolean isDead() {
        return dead;
    }
    
    boolean isValid() {
        if (!dead) return true;
        return System.currentTimeMillis() - deathTimeMs <= VALID_TIME_AFTER_DEATH_MS;
    }
    
    public static int getWidthOf(int invader) {
        return (int) INVADERS[invader][0].getWidth();
    }
    
    public static int getHeightOf(int invader) {
        return (int) INVADERS[invader][0].getHeight();
    }
    
}
