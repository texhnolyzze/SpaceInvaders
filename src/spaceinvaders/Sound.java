package spaceinvaders;

import java.io.File;
import kuusisto.tinysound.Music;
import kuusisto.tinysound.TinySound;

/**
 *
 * @author Texhnolyze
 */
public class Sound {

    static {
        TinySound.init();
    }
    
    private Music m;
    
    public Sound(String fileName) {
        this(fileName, 1.0D);
    }
    
    public Sound(String fileName, double vol) {
        m = TinySound.loadMusic(new File(fileName).getAbsoluteFile());
        m.setVolume(vol);
    }
    
    public void play(boolean loop) {
        m.rewind();
        m.play(loop);
    }
    
    public void stop() {
        m.stop();
    }
    
    public void fadeOut() {
        if (m.loop() && m.playing()) m.setLoop(false);
    }
    
}
