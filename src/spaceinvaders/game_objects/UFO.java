package spaceinvaders.game_objects;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import spaceinvaders.App;
import static spaceinvaders.game_objects.General.DIRECTION_LEFT;
import static spaceinvaders.game_objects.General.GAME_SPACE_LEFT_BOUND;
import static spaceinvaders.game_objects.General.GAME_SPACE_RIGHT_BOUND;

/**
 *
 * @author Texhnolyze
 */
public class UFO {
    
    private static final Image UFO = App.getImage("ufo");
    
    private int x;
    private final int y;
    private final int vel;
    private final int dir;
    private boolean destroyed;
    
    public UFO(int x, int y, int vel, int dir) {
        this.x = x;
        this.y = y;
        this.dir = dir;
        this.vel = vel;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public int getWidth() {
        return (int) UFO.getWidth();
    }
    
    public int getHeight() {
        return (int) UFO.getHeight();
    }
    
    public void move() {
        x += dir == DIRECTION_LEFT ? -vel : vel;
    }
    
    public void draw(GraphicsContext gc) {
        gc.drawImage(UFO, x, y);
    }
    
    public void handle(Shot s) {
        if (s.getType() != Shot.GUN_SHOT) return;
        int s_x = s.getX();
        int s_w = s.getWidth();
        int s_y = s.getY();
        int s_h = s.getHeight();
        int w = getWidth();
        int h = getHeight();
        if (General.isRectanglesOverlap(s_x, x, s_y, y, s_w, w, s_h, h)) {
            destroyed = true;
            s.hitted();
        }
    }
    
    public boolean isValid() {
        return !destroyed && dir == DIRECTION_LEFT ? 
                x > GAME_SPACE_LEFT_BOUND - getWidth() 
                : x < GAME_SPACE_RIGHT_BOUND + getWidth();
    }
    
    public static int getUFOWidth() {
        return (int) UFO.getWidth();
    }
    
}
