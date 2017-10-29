package spaceinvaders;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

/**
 *
 * @author Texhnolyze
 */
public class TextDrawingInParts {
    
    private int x;
    private int y;
    private int delay;
    private Font f;
    private String text = "";
    private TickTimer timer = new TickTimer();
    private TickOscillator flick;
    private TextOutputInParts parts;

    
    public TextDrawingInParts(String text, Font f, int delay, int x, int y) {
        this(text, f, delay, x, y, -1);
    }
    
    public TextDrawingInParts(String text, Font f, int delay, int x, int y, int flickFreq) {
        this.x = x;
        this.y = y;
        this.f = f;
        this.delay = delay;
        parts = new TextOutputInParts(text);
        if (flickFreq != -1) flick = new TickOscillator(flickFreq);
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public void draw(GraphicsContext gc, Paint p, TextDrawingInParts prev) {
        if (prev != null && prev.text.length() != prev.parts.getSrc().length()) 
            return;
        boolean draw;
        if (parts.getSrc().length() != text.length()) {
            draw = true;
            timer.tick();
            if (timer.passed(delay)) {
                timer.reset();
                text = parts.getPart();
                parts.next(true);
            }
        } else if (flick != null) {
            draw = flick.getState();
            flick.tick();
        } else draw = true;
        if (draw) {
            gc.setFill(p);
            gc.setFont(f);
            gc.fillText(text, x, y);
        }
    }
    
    public void reset() {
        parts.reset();
        timer.reset();
        if (flick != null) flick.reset();
        text = "";
    }
    
}
