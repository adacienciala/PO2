import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class hareBox extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        setScene(primaryStage, "serverScene.fxml", 600, 400);
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
    }

}
