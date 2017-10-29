package spaceinvaders;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author Texhnolyze
 */
public class App extends Application {

    private static final String IMAGES_DIRECTORY = loadImagesDirectory();
    private static final String SOUNDS_DIRECTORY = loadSoundsDirectory();

    private static final Font RETRO_FONT = loadFont();
    
    public static final int PIXEL_SIZE = 3; //same for width and height.
    
    public static final int CANVAS_WIDTH  = 230 * PIXEL_SIZE;
    public static final int CANVAS_HEIGHT = 185 * PIXEL_SIZE;
    
    private static final int WINDOW_WIDTH_OFFSET    = 6;
    private static final int WINDOW_HEIGHT_OFFSET   = 25;
    
    private static Stage stage;
    private static Canvas canvas;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)  {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        App.stage = stage;
        Group root = new Group();
        stage.setWidth(CANVAS_WIDTH + WINDOW_WIDTH_OFFSET);
        stage.setHeight(CANVAS_HEIGHT + WINDOW_HEIGHT_OFFSET);
        stage.setResizable(false);
        stage.setTitle("SpaceInvaders");
        Canvas canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        App.canvas = canvas;
        GraphicsContext gc = canvas.getGraphicsContext2D();
        root.getChildren().add(canvas);
        GameController controller = new GameController(gc);
        root.setOnKeyPressed(controller);
        root.setOnKeyReleased(controller);
        stage.setOnCloseRequest((WindowEvent event) -> {
            System.exit(0);
        });
        stage.setScene(new Scene(root, Color.BLACK));
        stage.show();
        root.requestFocus();
        controller.start();
    }
    
    public static int getWidth() {
        return (int) canvas.getWidth();
    }
    
    public static int getHeight() {
        return (int) canvas.getHeight();
    }
    
    public static final String getSoundFullPath(String name) {
        return SOUNDS_DIRECTORY + "/" + name + ".wav";
    }
    
    private static String getImageUrl(String name) {
        return new File(IMAGES_DIRECTORY + "/" + name + ".png").toURI().toString();
    }
    
    public static final Image getImage(String name) {
        Image src = new Image(getImageUrl(name));
        double w = src.getWidth() * PIXEL_SIZE;
        double h = src.getHeight() * PIXEL_SIZE;
        Image scaled = new Image(src.impl_getUrl(), w, h, false, false);
        return scaled;
    }
    
    public static final Font getRetroFontOf(double size) {
        return new Font(RETRO_FONT.getName(), size);
    }
    
    private static String loadImagesDirectory() {
        return new File("resources/img").getAbsolutePath();
    }
    
    private static String loadSoundsDirectory() {
        return new File("resources/sounds").getAbsolutePath();
    }
    
    private static Font loadFont() {
        Font f = null;
        try {
            f = Font.loadFont(new FileInputStream(new File("resources/font/8bit16.TTF").getAbsolutePath()), 1.0D);
        } catch (FileNotFoundException ex) {ex.printStackTrace();}
        return f;
    }
    
}
