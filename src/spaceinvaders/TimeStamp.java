package spaceinvaders;

/**
 *
 * @author Texhnolyze
 */
public class TimeStamp {

    private long stamp;
    
    public TimeStamp() {
        reset();
    }
    
    public boolean passed(long ms) {
        long l = paused ? cpy : System.currentTimeMillis() - stamp;
        return l >= ms;
    }
    
    public long getMsPassed() {
        long l = paused ? cpy : System.currentTimeMillis() - stamp;
        return l;
    }
    
    private long cpy;
    private boolean paused;
    
    public void pause() {
        if (!paused) {
            cpy = getMsPassed();
            paused = true;
        }
    }
    
    public void resume() {
        if (paused) {
            paused = false;
            stamp = System.currentTimeMillis() - cpy;
        }
    }
    
    public void reset() {
        stamp = System.currentTimeMillis();
    }
    
    public void correct(long delta) {
        stamp += delta;
    }
    
}
