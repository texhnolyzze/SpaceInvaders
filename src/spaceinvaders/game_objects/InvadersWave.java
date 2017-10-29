package spaceinvaders.game_objects;

import java.util.LinkedList;
import java.util.List;
import javafx.scene.canvas.GraphicsContext;
import spaceinvaders.App;
import static spaceinvaders.App.PIXEL_SIZE;
import spaceinvaders.GameController.InwadersWaveThemePlayer;
import spaceinvaders.TimeStamp;
import static spaceinvaders.game_objects.General.DIRECTION_LEFT;
import static spaceinvaders.game_objects.General.DIRECTION_RIGHT;
import static spaceinvaders.game_objects.General.GAME_SPACE_LEFT_BOUND;
import static spaceinvaders.game_objects.General.GAME_SPACE_RIGHT_BOUND;
import static spaceinvaders.game_objects.General.isIntervalsOverlap;
import static spaceinvaders.game_objects.Invader.INVADER_0;
import static spaceinvaders.game_objects.Invader.INVADER_1;
import static spaceinvaders.game_objects.Invader.INVADER_2;

/**
 *
 * @author Texhnolyze
 */
public class InvadersWave {
    
    private static final int MIN_DELAY = 50;
    
    private static int GLOBAL_FRAME_IDX;    
    
    private static final int WAVE_VELOCITY = 3 * App.PIXEL_SIZE;
    static final int WAVE_LEFT_BOUND = GAME_SPACE_LEFT_BOUND + WAVE_VELOCITY;
    static final int WAVE_RIGHT_BOUND = GAME_SPACE_RIGHT_BOUND - WAVE_VELOCITY;
    
    private static final int WAVE_COL_WIDTH = Invader.getWidthOf(INVADER_0);
    
    private int waveDirection;
    private final int initDirection;
    private final Invader[][] invaders;
    
    private int delay;
    private int delayDecrement;
    
    private int noteToPlayIdx;
    private final InwadersWaveThemePlayer player;
    
    private int invadersAliveCount;
    
    public InvadersWave(int delay, int delayDecrement, int waveDirection, int left_corner_x_pix, int left_corner_y_pix, InwadersWaveThemePlayer player) {
        if (delay < MIN_DELAY) throw new RuntimeException();
        this.delay = delay;
        this.delayDecrement = delayDecrement;
        this.waveDirection = initDirection = waveDirection;
        this.player = player;
        invaders = createInvadersWave(left_corner_x_pix * PIXEL_SIZE, left_corner_y_pix * PIXEL_SIZE);
        invadersAliveCount = invaders.length * invaders[0].length;
    }
    
    public void reset(int delay, int delayDecrement, int newY) {
        for (Invader[] invs : invaders) 
            for (Invader i : invs) i.reset(newY);
        invadersAliveCount = invaders.length * invaders[0].length;
        waveDirection = initDirection;
        this.delay = delay;
        this.delayDecrement = delayDecrement;
    }
    
    private static final int WAVE_COLS_NUM = 11;
    private static final int[] WAVE_COLUMN = {
        INVADER_2, INVADER_1, INVADER_1, INVADER_0, INVADER_0
    };
    
    private static final int[] INVADER_WIDTH_OFFSET_IN_COLUMN = {
        0, App.PIXEL_SIZE, 2 * App.PIXEL_SIZE
    };
    
    private static Invader[][] createInvadersWave(int x, int y) {
        int x_offset = x;
        int y_offset = y;
        Invader[][] invaders = new Invader[WAVE_COLS_NUM][WAVE_COLUMN.length];
        for (int col = 0; col < invaders.length; col++) {
            for (int row = 0; row < WAVE_COLUMN.length; row++) {
                int type = WAVE_COLUMN[row];
                int w = INVADER_WIDTH_OFFSET_IN_COLUMN[type];
                Invader i = new Invader(type, x_offset + w, y_offset);
                invaders[col][row] = i;
                y_offset += i.getHeight() + 7 * App.PIXEL_SIZE;
            }
            y_offset = y;
            x_offset += WAVE_COL_WIDTH + 3 * App.PIXEL_SIZE;
        }
        return invaders;
    }
    
    public void draw(GraphicsContext gc) {
        for (Invader[] invs : invaders)
            for (Invader i : invs) i.draw(gc, GLOBAL_FRAME_IDX);
    }
    
    private final TimeStamp prevMoved = new TimeStamp();
    
    public void update() {
        if (prevMoved.passed(delay)) {
            GLOBAL_FRAME_IDX = (GLOBAL_FRAME_IDX + 1) % 2;
            prevMoved.reset();
            player.addNote(noteToPlayIdx, (long) ((double) delay / 1.7D));
            noteToPlayIdx = (noteToPlayIdx + 1) % 4;
            if (waveDirection == DIRECTION_LEFT) {
                int leftmost = getLeftmostColX();
                if (leftmost > WAVE_LEFT_BOUND) move(-WAVE_VELOCITY, 0);
                else goDown();
            } else {
                int rightmost = getRightmostColX();
                if (rightmost + WAVE_COL_WIDTH < WAVE_RIGHT_BOUND) move(WAVE_VELOCITY, 0);
                else goDown();
            }
        }
    }
    
    public Invader handle(Shot s) {
        if (s.getType() == Shot.GUN_SHOT) {
            int col = getColIndexBy(s);
            if (col != -1) {
                for (int row = WAVE_COLUMN.length - 1; row >= 0; row--) {
                    Invader i = invaders[col][row];
                    if (!i.isDead()) {
                        i.handle(s);
                        if (!s.isValid()) {
                            invadersAliveCount--;
                            delay -= 2;
                            return i;
                        }
                    }
                }
            }
        }
        return null;
    }
    
    public boolean isDestroyed() {
        return invadersAliveCount == 0;
    }
    
    private int getColIndexBy(Shot s) {
        int s_x = s.getX();
        int s_w = s.getWidth();
        for (int col = 0; col < invaders.length; col++) {
            int col_x = getColX(col);
            if (col_x != -1) {
                if (isIntervalsOverlap(s_x, s_x + s_w, col_x, col_x + WAVE_COL_WIDTH)) 
                    return col;
            }
        }
        return -1;
    }
    
    private void goDown() {
        move(0, WAVE_VELOCITY);
        delay = Math.max(MIN_DELAY, delay - delayDecrement);
        waveDirection = waveDirection == DIRECTION_LEFT ? DIRECTION_RIGHT : DIRECTION_LEFT;
    }
    
    public List<Shot> getShots() {
        List<Shot> shots = null;
        for (int col = 0; col < invaders.length; col++) {
            Invader i = getFirstInvaderInTheCol(col);
            if (i != null) {
                Shot s = i.bang(invadersAliveCount);
                if (s != null) {
                    if (shots == null) shots = new LinkedList<>();
                    shots.add(s);
                }
            }
        }
        return shots;
    }
    
    private Invader getFirstInvaderInTheCol(int col) {
        for (int row = WAVE_COLUMN.length - 1; row >= 0; row--) {
            Invader inv = invaders[col][row];
            if (!inv.isDead()) return inv;
        }
        return null;
    }
    
    private void move(int vel_x, int vel_y) {
        for (Invader[] invs : invaders)
            for (Invader i : invs) i.move(vel_x, vel_y);
    }
    
    private int getLeftmostColX() {
        for (int col = 0; col < invaders.length; col++) 
            for (int row = 0; row < WAVE_COLUMN.length; row++)
                if (!invaders[col][row].isDead()) return getColX(col);
        return -1;
    }
    
    private int getRightmostColX() {
        for (int col = invaders.length - 1; col >= 0; col--) 
            for (int row = 0; row < WAVE_COLUMN.length; row++)
                if (!invaders[col][row].isDead()) return getColX(col);
        return -1;
    }
    
    public Invader getBottomMost() {
        for (int row = WAVE_COLUMN.length - 1; row >= 0; row--) {
            for (Invader[] invader : invaders) {
                if (!invader[row].isDead()) {
                    return invader[row];
                }
            }
        }
        return null;
    }
    
    private int getColX(int col) {
        for (int i = WAVE_COLUMN.length - 1; i >= 0; i--) {
            Invader inv = invaders[col][i];
            if (!inv.isDead()) 
                return inv.getX() - INVADER_WIDTH_OFFSET_IN_COLUMN[inv.getType()];        
        }
        return -1;
    }
    
}
