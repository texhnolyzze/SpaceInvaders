package spaceinvaders;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.AnimationTimer;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Pair;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;
import static spaceinvaders.App.PIXEL_SIZE;
import spaceinvaders.game_objects.General;
import static spaceinvaders.game_objects.General.DIRECTION_LEFT;
import static spaceinvaders.game_objects.General.DIRECTION_NONE;
import static spaceinvaders.game_objects.General.DIRECTION_RIGHT;
import spaceinvaders.game_objects.Gun;
import spaceinvaders.game_objects.Invader;
import spaceinvaders.game_objects.InvadersWave;
import spaceinvaders.game_objects.Shield;
import spaceinvaders.game_objects.Shot;
import spaceinvaders.game_objects.UFO;

/**
 *
 * @author Texhnolyze
 */
public class GameController extends AnimationTimer implements EventHandler<KeyEvent> {
    
    private static InwadersWaveThemePlayer player;
    
    private static final Random RANDOM = new Random();
    
    private static final int MENU = 0;
    private static final int PLAYING = 1;
    private static final int GAME_OVER = 2;
    
    private final GraphicsContext gc;
    
    private Player p;
    
    private Gun gun;
    private InvadersWave wave;
    private Shield[] shields;
    private UFO ufo;
    
    private int state;
    
    private List<Shot> shots = new LinkedList<>();
    
    public GameController(GraphicsContext gc) throws MidiUnavailableException {
        this.gc = gc;
        player = new InwadersWaveThemePlayer();
    }
    
    private static final int IN_THE_CENTER           = 1;
    private static final int IN_THE_LEFT_SIDE        = 2;
    private static final int IN_THE_RIGHT_SIDE      = 3;
    
    private static int centerText(String str, Font f, int how) {
        Text text = new Text(str);
        text.setFont(f);
        Bounds b = text.getBoundsInLocal();
        int x_offset = (int) (b.getMaxX() / 2);
        int base = (int) (App.getWidth() / (how == IN_THE_CENTER ? 2 
                : how == IN_THE_LEFT_SIDE ? 4 : (4D / 3D)));
        return base - x_offset;
    }
    
    private void clearView() {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, App.getWidth(), App.getHeight());
    }
    
    private void drawFrame() {
        gc.setFill(Color.CHARTREUSE);
        gc.fillRect(0, 0, PIXEL_SIZE, App.getHeight());
        gc.fillRect(0, 0, App.getWidth(), PIXEL_SIZE);
        gc.fillRect(0, App.getHeight() - PIXEL_SIZE, App.getWidth(), PIXEL_SIZE);
        gc.fillRect(App.getWidth() - PIXEL_SIZE, 0, PIXEL_SIZE, App.getHeight());
    }
    
    private void drawGameName() {
        SPACE_DRAWING.draw(gc, Color.LAWNGREEN, null);
        INVADERS_DRAWING.draw(gc, Color.LAWNGREEN, SPACE_DRAWING);
    }
    
    private void drawPressToPlay() {
        PRESS_TO_PLAY_DRAWING.draw(gc, Color.WHITE, INVADERS_DRAWING);
    }
    
    private int note;
    private int animIdx;
    private TimeStamp stamp = new TimeStamp();
    
    private void drawMenuInvaders() {
        final boolean b = stamp.passed(900);
        if (b) animIdx = (animIdx + 1) % 2;
        for (Invader i : MENU_INVADERS) i.draw(gc, animIdx);
        if (b) {
            player.addNote(note, 300);
            stamp.reset();
            note = (note + 1) % 4;
        }
    }
    
    private TimeStamp ufoEmergence = new TimeStamp();
    
    private void drawUpdateMenuUFO() {
        if (ufo != null) {
            ufo.draw(gc);
            ufo.move();
            if (!ufo.isValid()) {
                ufo = null;
                ufoEmergence.reset();
                SoundStore.UFO.fadeOut();
            }
        } else if (ufoEmergence.passed(5500)) {
            ufo = new UFO(
                    0 - UFO.getUFOWidth(), 
                    (int) (App.getHeight() / 1.2D), 2, 
                    DIRECTION_RIGHT
            );
            SoundStore.UFO.play(true);
        }
    }
    
    private static final Font HUD_FONT = App.getRetroFontOf(App.PIXEL_SIZE * 7);
    private static final int SCORE_X = centerText("SCORE: 0", HUD_FONT, IN_THE_LEFT_SIDE);
    private static final int LIVES_X = centerText("LIVES: 3", HUD_FONT, IN_THE_RIGHT_SIDE);
    
    private void drawGameHUD() {
        gc.setFont(HUD_FONT);
        gc.setFill(Color.WHITE);
        gc.fillText("SCORE: " + p.getScore(), SCORE_X, App.getHeight() / 13.0D);
        gc.fillText("LIVES: " + p.getLifesCount(), LIVES_X, App.getHeight() / 13.0D);
    }
    
    private static final int[][] STARS = initStars();
    
    private static int[][] initStars() {
        int[][] stars = new int[30][2];
        for (int i = 0; i < stars.length; i++) {
            stars[i][0] = RANDOM.nextInt(App.getWidth());
            stars[i][1] = RANDOM.nextInt(App.getHeight());
        }
        return stars;
    }
    
    private void drawStars() {
        gc.setFill(Color.WHITE);
        for (int i = 0; i < STARS.length; i++) {
            gc.fillRect(STARS[i][0], STARS[i][1], 1, 1);            
        }
    }
    
    private static final int WAVE_INIT_DELAY = 600;
    private static final int WAVE_DELAY_DECREMENT = 50;
    private static final int INIT_WAVE_Y = 158 >> 2;
    private static final int MAX_WAVE_DY = 5 * PIXEL_SIZE;
    
    private int currentWaveDelay;
    private int currentDelayDecrement;
    private int currentWaveDy;
    
    private void newGame() {
        ufo = null;
        ufoEmergence.reset();
        SoundStore.UFO.stop();
        p = new Player(3);
        gun = new Gun(25, 172, 2);
        int shield_x = 82 >> 2;
        int shield_y = 562 >> 2;
        shields = new Shield[4];
        for (int i = 0; i < shields.length; i++) {
            Shield s = new Shield(shield_x, shield_y);
            shields[i] = s;
            shield_x += (s.getWidth() / PIXEL_SIZE) + 30;
        }
        wave = new InvadersWave(
                WAVE_INIT_DELAY, WAVE_DELAY_DECREMENT, 
                DIRECTION_RIGHT, 140 >> 2, INIT_WAVE_Y, player
        );
        currentWaveDelay = WAVE_INIT_DELAY;
        currentDelayDecrement = WAVE_DELAY_DECREMENT;
        currentWaveDy = 0;
        state = PLAYING;
    }
    
    private boolean playerDead;
    private boolean waveDestroyed;
    private final TimeStamp playerDeadStamp = new TimeStamp();
    private final TimeStamp waveDestroyedStamp = new TimeStamp();
    
    private int ufoDestroyedScoreIncrease;
    private boolean ufoDestroyedDrawScore;    
    private int ufoDestroyedX, ufoDestroyedY;
    private final TimeStamp ufoDestroyedStamp = new TimeStamp();
    
    @Override
    //MAIN LOOP
    public void handle(long now) {
        clearView();
        switch (state) {
            case MENU:
                drawMenuInvaders();
                drawGameName();
                drawPressToPlay();
                drawUpdateMenuUFO();
                break;
            case PLAYING:
                drawGameHUD();
                if (playerDead) {
                    drawAll(false);
                    if (playerDeadStamp.passed(1000)) {
                        SoundStore.GUN_EXPLOSE.fadeOut();
                        playerDead = false;
                        if (p.getLifesCount() == 0) {
                            state = GAME_OVER;
                            String s = "SCORE: " + p.getScore();
                            gameOverScore = new TextDrawingInParts(
                                    s, HUD_FONT, 10,
                                    centerText(s, HUD_FONT, IN_THE_CENTER),
                                    (int) (App.getHeight() / 1.8D)
                            );
                            SoundStore.UFO.stop();
                        } else {
                            gun.reset();
                            shots.clear();
                        }
                    }
                } else {
                    drawAll(true);
                    updateAll();
                    shotsHandle();
                    if (ufo == null && RANDOM.nextInt(2500) == 37) {
                        ufo = new UFO(0 - UFO.getUFOWidth(),
                                (int) (App.getHeight() / 10D), 2, DIRECTION_RIGHT);
                        SoundStore.UFO.play(true);
                    }
                    if (ufoDestroyedDrawScore) {
                        drawUFOShootingIncreaseScore();
                        if (ufoDestroyedStamp.passed(1500)) ufoDestroyedDrawScore = false;
                    }
                    if (waveDestroyed) {
                        if (waveDestroyedStamp.passed(1000)) {
                            if (p.getLifesCount() < 3) p.extraLife();
                            waveDestroyed = false;
                            gun.reset();
                            currentWaveDelay = Math.max(currentWaveDelay - 20, 350);
                            currentDelayDecrement = Math.min(currentDelayDecrement + 7, 70);
                            int i = currentWaveDy + PIXEL_SIZE;
                            currentWaveDy = i == MAX_WAVE_DY ? 0 : i;
                            wave.reset(
                                    currentWaveDelay, currentDelayDecrement, currentWaveDy
                            );
                        }
                    } else if (isPlayerLose()) {
                        String s = "SCORE: " + p.getScore();
                        gameOverScore = new TextDrawingInParts(
                                s, HUD_FONT, 10,
                                centerText(s, HUD_FONT, IN_THE_CENTER),
                                (int) (App.getHeight() / 1.5D)
                        );
                        SoundStore.UFO.stop();
                        state = GAME_OVER;
                    }
                }   break;
            default:
                drawGameOver();
                break;
        }
        drawFrame();
        drawStars();
    }
    
    //.....................//
        private static final String GAME_OVER_STR = "GAME OVER";
        private static final Font GAME_OVER_FONT = App.getRetroFontOf(15 * PIXEL_SIZE);
        private static final int GAME_OVER_X = centerText(
                GAME_OVER_STR, GAME_OVER_FONT, IN_THE_CENTER
        );
        private static final TextDrawingInParts GAME_OVER_DRAWING = new TextDrawingInParts(
            GAME_OVER_STR, GAME_OVER_FONT, 10, GAME_OVER_X, (int) (App.getHeight() / 2.5D), 10
        );
        private TextDrawingInParts gameOverScore;
    //.....................//
    
    private void drawGameOver() {
        GAME_OVER_DRAWING.draw(gc, Color.RED, null);
        gameOverScore.draw(gc, Color.WHITE, GAME_OVER_DRAWING);
    }

    private void drawAll(boolean animateShots) {
        gun.draw(gc);
        wave.draw(gc);
        if (ufo != null) ufo.draw(gc);
        for (Shot s : shots) s.draw(gc, animateShots);
        for (Shield s : shields) s.draw(gc);
    }
    
    private final TickOscillator ufoShootingFlickering = new TickOscillator(3);
    
    private void drawUFOShootingIncreaseScore() {
        if (ufoShootingFlickering.getState()) {
            gc.setFill(Color.WHITE);
            gc.setFont(HUD_FONT);
            gc.fillText(ufoDestroyedScoreIncrease + "", ufoDestroyedX, ufoDestroyedY);
        }
        ufoShootingFlickering.tick();
    }
    
    private void updateAll() {
        gun.move();
        wave.update();
        List<Shot> waveShots = wave.getShots();
        if (waveShots != null) shots.addAll(waveShots);
        if (ufo != null) {
            ufo.move();
            if (!ufo.isValid()) {
                ufo = null;
                SoundStore.UFO.fadeOut();
            }
        }
        for (Shot s : shots) s.move();
    }
    
    private static final int[] INVADER_KILLING_INCREASE_SCORE = {10, 20, 40};
    private static final int[] UFO_SHOOTING_INCREASE_SCORE = {
        50, 100, 150, 200, 350, 400, 450
    };
    
    private void shotsHandle() {
        Iterator<Shot> it = shots.iterator();
        while (it.hasNext()) {
            Shot s = it.next();
            for (Shield shield : shields) {
                shield.handle(s);
                if (!s.isValid()) {
                    it.remove();
                    break;
                }
            }
        }
        it = shots.iterator();
        while (it.hasNext()) {
            Shot s = it.next();
            Invader i = wave.handle(s);
            if (i != null) {
                it.remove();
                p.increaseScore(INVADER_KILLING_INCREASE_SCORE[i.getType()]);
                if (wave.isDestroyed()) {
                    waveDestroyed = true;
                    waveDestroyedStamp.reset();
                    break;
                }
            }
        }
        it = shots.iterator();
        while (it.hasNext()) {
            Shot s = it.next();
            boolean b = gun.isDestroyedBy(s);
            if (b) {
                playerDead = true;
                playerDeadStamp.reset();
                p.decreaseLifesCount();
                it.remove();
                break;
            }
        }
        if (ufo != null) {
            it = shots.iterator();
            while (it.hasNext()) {
                Shot s = it.next();
                ufo.handle(s);
                if (!s.isValid()) {
                    SoundStore.UFO.stop();
                    SoundStore.UFO_EXPLOSION.play(false);
                    int score = UFO_SHOOTING_INCREASE_SCORE[
                            RANDOM.nextInt(
                                    UFO_SHOOTING_INCREASE_SCORE.length
                            )
                    ];
                    p.increaseScore(score);
                    ufoDestroyedDrawScore = true;
                    ufoDestroyedScoreIncrease = score;
                    ufoDestroyedX = ufo.getX();
                    ufoDestroyedY = ufo.getY();
                    ufoDestroyedStamp.reset();
                    ufo = null;
                    break;
                }
            }
        }
    }
    
    private boolean isPlayerLose() {
        Invader i = wave.getBottomMost();
        int y = i.getY() + Invader.getHeightOf(i.getType());
        return y > General.GAME_SPACE_LOWER_BOUND;
    }
    
    @Override
    public void handle(KeyEvent event) {
        KeyCode c = event.getCode();
        switch (state) {
            case MENU:
                if (event.getEventType() == KeyEvent.KEY_PRESSED) newGame();
                break;
            case PLAYING:
                if (event.getEventType() == KeyEvent.KEY_PRESSED) {
                    if (c == KeyCode.LEFT) gun.setDirection(DIRECTION_LEFT);
                    else if (c == KeyCode.RIGHT) gun.setDirection(DIRECTION_RIGHT);
                } else {
                    if (!gun.isDestroyed()) {
                        if (c == KeyCode.LEFT || c == KeyCode.RIGHT)
                            gun.setDirection(DIRECTION_NONE);
                        else if (c == KeyCode.SPACE) {
                            Shot s = gun.bang();
                            if (s != null) shots.add(s);
                        }
                    }
                }   break;
            default:
                GAME_OVER_DRAWING.reset();
                SPACE_DRAWING.reset();
                INVADERS_DRAWING.reset();
                PRESS_TO_PLAY_DRAWING.reset();
                ufo = null;
                ufoEmergence.reset();
                state = MENU;
                break;
        }
    }
    
    //-------MENU VIEW--------//
    
    private static final String SPACE = "SPACE";
    private static final String INVADERS = "INVADERS";
    private static final String PRESS_TO_PLAY = "PRESS TO PLAY";
    private static final Font GAME_NAME_FONT = App.getRetroFontOf(16 * PIXEL_SIZE);
    private static final Font PRESS_TO_PLAY_FONT = App.getRetroFontOf(10 * PIXEL_SIZE);
    private static final TextDrawingInParts SPACE_DRAWING = 
            new TextDrawingInParts(
                    SPACE, GAME_NAME_FONT, 
                    10, centerText(SPACE, GAME_NAME_FONT, IN_THE_CENTER), 
                    (int) (App.getHeight() / 3.5D)
    );
    private static final TextDrawingInParts INVADERS_DRAWING = 
            new TextDrawingInParts(
                    INVADERS, GAME_NAME_FONT,
                    10, centerText(INVADERS, GAME_NAME_FONT, IN_THE_CENTER),
                    (int) (SPACE_DRAWING.getY() + App.getHeight() / 8.0D)
    ); 
    private static final TextDrawingInParts PRESS_TO_PLAY_DRAWING = 
            new TextDrawingInParts(
                    PRESS_TO_PLAY, PRESS_TO_PLAY_FONT, 
                    10, centerText(PRESS_TO_PLAY, PRESS_TO_PLAY_FONT, IN_THE_CENTER), 
                    (int) (INVADERS_DRAWING.getY() + App.getHeight() / 6.0D), 20
    );
    
    private static final Invader[] MENU_INVADERS = createMenuInvaders();
    
    private static Invader[] createMenuInvaders() {
        int y = (int) (SPACE_DRAWING.getY() - App.getHeight() / 5.0D);
        int x_1 = (int) (App.getWidth() / 2 - App.getWidth() / (7.6D));
        Invader i_1 = new Invader(Invader.INVADER_0, x_1, y);
        int x_2 = (int) (i_1.getX() + App.getWidth() / 9.3D);
        Invader i_2 = new Invader(Invader.INVADER_1, x_2, y);
        int x_3 = (int) (i_2.getX() + App.getWidth() / 9.3D);
        Invader i_3 = new Invader(Invader.INVADER_2, x_3, y);        
        return new Invader[] {i_1, i_2, i_3};
    }
    
    //--------------------------//
    
    public static class InwadersWaveThemePlayer extends Thread {

        private static final int VOLUME = 127;
        
        private static final int[] MAIN_THEME_NOTES = {
            36, 35, 34, 33
        };
        
        private final Queue<Pair<Integer, Long>> notesToPlay = new ConcurrentLinkedQueue<>();
        
        private final MidiChannel channel;
        
        private InwadersWaveThemePlayer() throws MidiUnavailableException {
            Synthesizer synth = MidiSystem.getSynthesizer();
            synth.open();
            MidiChannel[] channels = synth.getChannels();
            channel = channels[0];
            channel.programChange(39);
            this.start();
        }
        
        public synchronized void addNote(int noteIdx, long durationMs) {
            notesToPlay.add(new Pair<>(noteIdx, durationMs));
            notify();
        }
        
        @Override
        public synchronized void run() {
            while (true) {
                try {
                    if (notesToPlay.isEmpty()) wait();
                    else {
                        Pair<Integer, Long> note = notesToPlay.poll();
                        int n = MAIN_THEME_NOTES[note.getKey()];
                        long dur = note.getValue();
                        channel.noteOn(n, VOLUME);
                        Thread.sleep(dur);
                        channel.noteOff(n);
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(InvadersWave.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
    }
    
}
