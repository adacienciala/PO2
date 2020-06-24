import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class hareBox extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("loginScene.fxml"));
        primaryStage.setTitle("hareBox");
        primaryStage.setMinHeight(350);
        primaryStage.setMinWidth(350);
        primaryStage.setScene(new Scene(root));
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream(".\\resources\\hareBoxIconBlack.png")));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
