import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * <code>hareBox</code> objects is responsible for the application. Sets the stage and scenes.
 *
 * @author Adrianna Cieńciała
 * @version 1.0
 */
public class hareBox extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        setScene(primaryStage, "serverScene.fxml", 600, 400);
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("hareBoxIconBlack.png")));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void setScene(Stage primaryStage, String sceneFile, int width, int height) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource(sceneFile));
        primaryStage.setTitle(this.getClass().getSimpleName());
        primaryStage.setScene(new Scene(root, width, height));
        primaryStage.setMinWidth(width);
        primaryStage.setMinHeight(height);
        primaryStage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });
    }

}
