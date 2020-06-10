package hareBoxClient;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class LoginSceneController {
    public Pane loginPane;
    public ImageView logoImg;
    public JFXButton signInBtn;
    public JFXButton dirButton;
    public JFXTextField pathTF;
    public JFXTextField usernameTF;
    private Stage window;

    public void signIn(ActionEvent actionEvent) throws IOException {
        boolean incorrectData = false;
        if (pathTF.getText().equals("")) {
            pathTF.setPromptText("Incorrect path!");
            pathTF.setUnFocusColor(Paint.valueOf("red"));
            pathTF.setFocusColor(Paint.valueOf("red"));
            incorrectData = true;
        }
        else {
            pathTF.setPromptText("Local path");
            pathTF.setUnFocusColor(Paint.valueOf("white"));
            pathTF.setFocusColor(Paint.valueOf("white"));
        }
        if (usernameTF.getText().equals("")) {
            usernameTF.setPromptText("Incorrect username!");
            usernameTF.setUnFocusColor(Paint.valueOf("red"));
            usernameTF.setFocusColor(Paint.valueOf("red"));
            incorrectData = true;
        }
        else {
            usernameTF.setPromptText("Username");
            usernameTF.setUnFocusColor(Paint.valueOf("white"));
            usernameTF.setFocusColor(Paint.valueOf("white"));
        }
        if (incorrectData)
            return;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("clientScene.fxml"));

        window = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
        window.setMinHeight(350);
        window.setMinWidth(450);
        window.setHeight(400);
        window.setWidth(600);
        window.setScene(new Scene(loader.load()));
        window.centerOnScreen();

        ClientSceneController controller = loader.<ClientSceneController>getController();
        controller.initData(usernameTF.getText(), pathTF.getText());

        window.show();
    }

    public void chooseDirectory(ActionEvent actionEvent) {
        DirectoryChooser dirChooser = new DirectoryChooser();
        File selectedDirectory = dirChooser.showDialog(window);
        if (selectedDirectory != null)
            pathTF.setText(selectedDirectory.getAbsolutePath());
    }
}
