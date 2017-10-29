package spaceinvaders.game_objects;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import spaceinvaders.App;
import static spaceinvaders.App.PIXEL_SIZE;

/**
 *
 * @author Texhnolyze
 */
public class Shield {
    
    private static final int WHOLE_BLOCK                = 0;
    private static final int UPPER_LEFT_CORNER_BLOCK    = 1;
    private static final int UPPER_RIGHT_CORNER_BLOCK   = 2;
    private static final int LOWER_RIGHT_CORNER_BLOCK   = 3;
    private static final int LOWER_LEFT_CORNER_BLOCK    = 4;
    
    private static final int[][] SHIELD = {
         {LOWER_RIGHT_CORNER_BLOCK, WHOLE_BLOCK, WHOLE_BLOCK, LOWER_LEFT_CORNER_BLOCK},
         {WHOLE_BLOCK, UPPER_LEFT_CORNER_BLOCK, UPPER_RIGHT_CORNER_BLOCK, WHOLE_BLOCK},
         {WHOLE_BLOCK,              -1,             -1,                   WHOLE_BLOCK}
    };    
    
    private static final int BLOCK_LIVES = 4;
    
    private static final Image[][] BLOCKS = {
        {
            App.getImage("whole_block"), 
            App.getImage("whole_block_shooted_0"), 
            App.getImage("whole_block_shooted_1"), 
            App.getImage("whole_block_shooted_2")
        },
        {
            App.getImage("block_0"),
            App.getImage("block_0_shooted_0"),
            App.getImage("block_0_shooted_1"),
            App.getImage("block_0_shooted_2"),
        },
        {
            App.getImage("block_1"),
            App.getImage("block_1_shooted_0"),
            App.getImage("block_1_shooted_1"),
            App.getImage("block_1_shooted_2"),
        },
        {
            App.getImage("block_2"),
            App.getImage("block_2_shooted_0"),
            App.getImage("block_2_shooted_1"),
            App.getImage("block_2_shooted_2"),
        },
        {
            App.getImage("block_3"),
            App.getImage("block_3_shooted_0"),
            App.getImage("block_3_shooted_1"),
            App.getImage("block_3_shooted_2"),
        }
    };
    
    private final int x;
    private final int y;
    
    private final Block[][] blocks;
    
    public Shield(int x_pix, int y_pix) {
        this.x = x_pix * PIXEL_SIZE;
        this.y = y_pix * PIXEL_SIZE;
        blocks = createBlocks(x, y);
    }
    
    public void reset() {
        for (Block[] bs : blocks)
            for (Block b : bs) b.reset();
        
    }
    
    private static Block[][] createBlocks(int x, int y) {
        Block[][] blocks = new Block[SHIELD.length][SHIELD[0].length];
        int x_offset = x;
        int y_offset = y;
        for (int row = 0; row < blocks.length; row++) {
            for (int col = 0; col < blocks[row].length; col++) {
                Block b = blocks[row][col] = new Block(
                        x_offset, 
                        y_offset, 
                        SHIELD[row][col]
                );
                x_offset += b.getWidth();
            }
            x_offset = x;
            y_offset += blocks[row][0].getHeight();
        }
        return blocks;
    }

    public int getWidth() {
        int w = 0;
        for (int col = 0; col < blocks[0].length; col++)
            w += blocks[0][col].getWidth();
        return w;
    }
    
    public void draw(GraphicsContext gc) {
        for (Block[] bs : blocks) 
            for (Block b : bs) 
                b.draw(gc);
    }
    
    public void handle(Shot s) {
        int s_x = s.getX();
        int s_w = s.getWidth();
        int s_y = s.getY();
        int s_h = s.getHeight();
        outer: for (int row = 0; row < blocks.length; row++) {
            for (int col = 0; col < blocks[row].length; col++) {
                Block b = blocks[row][col];
                if (b.destroyed) continue;
                int b_w = b.getWidth();
                int b_h = b.getHeight();
                if (General.isRectanglesOverlap(s_x, b.x, s_y, b.y, s_w, b_w, s_h, b_h)) {
                    b.hitted();
                    s.hitted();
                    break outer;
                }
            }
        }
    }
    
    static private class Block {
        
        final int x;
        final int y;

        private int lives = BLOCK_LIVES;
        
        private final int type;
        private boolean destroyed;
        
        Block(int x, int y, int type) {
            this.type = type;
            if (type == -1) destroyed = true;
            this.x = x;
            this.y = y;
        }
        
        void reset() {
            destroyed = false;
            lives = BLOCK_LIVES;
        }
        
        void hitted() {
            lives--;
            if (lives == 0) destroyed = true;
        }
        
        void draw(GraphicsContext gc) {
            if (type != -1) {
                if (!destroyed) 
                    gc.drawImage(BLOCKS[type][BLOCK_LIVES - lives], x, y);
            }
        }
        
        int getWidth() {
            if (type == -1) return (int) BLOCKS[0][0].getWidth();
            else return (int) BLOCKS[type][0].getWidth();
        }
        
        int getHeight() {
            if (type == -1) return (int) BLOCKS[0][0].getHeight();
            else return (int) BLOCKS[type][0].getHeight();
        }
        
    }
    
}
