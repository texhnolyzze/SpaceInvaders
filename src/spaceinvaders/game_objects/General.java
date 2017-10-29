package spaceinvaders.game_objects;

import spaceinvaders.App;

/**
 *
 * @author Texhnolyze
 */
public final class General {
    
    public static final int DIRECTION_NONE  = -1;
    public static final int DIRECTION_LEFT  =  0;
    public static final int DIRECTION_RIGHT =  1;
    

    public static final int GAME_SPACE_LEFT_BOUND = App.PIXEL_SIZE;
    public static final int GAME_SPACE_RIGHT_BOUND = App.CANVAS_WIDTH - App.PIXEL_SIZE;
    public static final int GAME_SPACE_UPPER_BOUND = App.PIXEL_SIZE;
    public static final int GAME_SPACE_LOWER_BOUND = App.CANVAS_HEIGHT - App.PIXEL_SIZE;
    
    static final boolean isRectanglesOverlap(int x_1, int x_2, int y_1, int y_2, int w_1, int w_2, int h_1, int h_2) {
        int a_x_1 = x_1;
        int b_x_1 = x_1 + w_1;
        int a_x_2 = x_2;
        int b_x_2 = x_2 + w_2;
        if (!isIntervalsOverlap(a_x_1, b_x_1, a_x_2, b_x_2)) return false;
        int a_y_1 = y_1;
        int b_y_1 = y_1 + h_1;
        int a_y_2 = y_2;
        int b_y_2 = y_2 + h_2;
        return isIntervalsOverlap(a_y_1, b_y_1, a_y_2, b_y_2);
    }
    
    public static boolean isIntervalsOverlap(int a_1, int b_1, int a_2, int b_2) {
        return Math.max(a_1, a_2) < Math.min(b_1, b_2);
    }
    
}
