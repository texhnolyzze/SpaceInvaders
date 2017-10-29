package spaceinvaders.game_objects;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import spaceinvaders.App;
import spaceinvaders.TickTimer;
import static spaceinvaders.game_objects.General.GAME_SPACE_LOWER_BOUND;
import static spaceinvaders.game_objects.General.GAME_SPACE_UPPER_BOUND;
/**
 *
 * @author Texhnolyze
 */
public class Shot {

    public static final int GUN_SHOT = 0;
    public static final int INVADERS_SHOT_1 = 1;
    public static final int INVADERS_SHOT_2 = 2;
    
    public static final int MAX_VELOCITY = Invader.getHeightOf(Invader.INVADER_2);
    
    private static final Image[][] SHOTS = {
        {
            App.getImage("shot_0_0")
        },
        {
            App.getImage("shot_1_0"),
            App.getImage("shot_1_1"),
            App.getImage("shot_1_2"),
            App.getImage("shot_1_3"),
            App.getImage("shot_1_4"),
            App.getImage("shot_1_5")
        },
        {
            App.getImage("shot_2_0"),
            App.getImage("shot_2_1")
        },
    };
    
    private int y;
    private final float x;
    private final float velocity;
    
    private final int type;
    private int frameIdx; //need only for invaders shots.
    
    public Shot(int x, int y, float vel, int type) {
        if (vel > MAX_VELOCITY) throw new RuntimeException();
        this.velocity = vel;
        this.type = type;
        this.x = x;
        this.y = y;
    }
    
    public int getType() {
        return type;
    }
    
    public int getX() {
        return (int) x;
    }
    
    public int getY() {
        return y;
    }
    
    public int getWidth() {
        return (int) SHOTS[type][frameIdx].getWidth();
    }
    
    public int getHeight() {
        return (int) SHOTS[type][frameIdx].getHeight();
    }
    
    public void move() {
        if (type == GUN_SHOT) y -= velocity;
        else y += velocity;
    }
    
    public void draw(GraphicsContext gc, boolean anim) {
        gc.drawImage(SHOTS[type][frameIdx], x, y);
        if (anim) updateFrameIdx();
    }
    
    private boolean hited;
    
    void hitted() {
        hited = true;
    }
    
    private boolean increment = true; 
    
    private TickTimer timer = new TickTimer();
    
    private void updateFrameIdx() {
        if (type != GUN_SHOT) {
            timer.tick();
            if (timer.passed(3)) {
                timer.reset();
                int lim = increment ? SHOTS[type].length - 1 : 0;
                if (increment) frameIdx++;
                else frameIdx--;
                if (frameIdx == lim) increment = !increment;
            }
        }
    }
    
    public boolean isValid() {
        if (hited) return false;
        if (type == GUN_SHOT) {
            return y >= GAME_SPACE_UPPER_BOUND - getHeight();
        } else return y <= GAME_SPACE_LOWER_BOUND;
    }
    
    static int getWidthOf(int shot) {
        return (int) SHOTS[shot][0].getWidth();
    }
    
    static int getHeightOf(int shot) {
        return (int) SHOTS[shot][0].getHeight();
    }
    
}
