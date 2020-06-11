package hareBoxClient;

import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javafx.event.ActionEvent;

import java.io.File;
import java.io.IOException;

public class ClientSceneController {

    public JFXButton signOutBtn;
    public File userDir;
    public Label userDirLabel;
    public Label usernameLabel;
    public TextField logTF;

    private ClientUnit clientUnit;

    public void signOut(ActionEvent actionEvent) throws IOException {
        clientUnit.outputStream.close();

        Parent hareBoxParent = FXMLLoader.load(getClass().getResource("loginScene.fxml"));
        Stage window = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
        window.setMinHeight(350);
        window.setMinWidth(350);
        window.setHeight(350);
        window.setWidth(350);
        window.setScene(new Scene(hareBoxParent));
        window.centerOnScreen();
        window.show();
    }

    void initData(String username, String userPath)
    {
        this.userDir = new File(userPath);
        userDirLabel.setText(userPath);
        usernameLabel.setText(username);
        try {
            clientUnit = new ClientUnit(5000, username, userDir, logTF);
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        if (logTF.getText().equals(""))
            logTF.setText("Logged in!");
    }

}
