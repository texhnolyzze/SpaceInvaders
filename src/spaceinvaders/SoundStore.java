package spaceinvaders;

/**
 *
 * @author Texhnolyze
 */
public class SoundStore {

    public static final Sound GUN_SHOOT = new Sound(App.getSoundFullPath("gun_shoot"), 0.1D);
    public static final Sound GUN_EXPLOSE = new Sound(App.getSoundFullPath("gun_explose"), 0.1D);
    public static final Sound INVADER_KILLED = new Sound(App.getSoundFullPath("invader_killed"), 0.1);
    public static final Sound UFO = new Sound(App.getSoundFullPath("ufo"), 0.2D);
    public static final Sound UFO_EXPLOSION = new Sound(App.getSoundFullPath("ufo_explosion"), 0.3);
    
}
