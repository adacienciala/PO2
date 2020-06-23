import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

import javafx.event.ActionEvent;

import java.io.File;
import java.io.IOException;

public class ClientSceneController {

    public JFXButton signOutBtn;
    public Label userDirLabel;
    public Label usernameLabel;
    public TextField logTF;
    public JFXListView<String> filesListView;

    public File userDir;
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
            linkListToFileList();
            clientUnit.start();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        if (logTF.getText().equals(""))
            logTF.setText("Logged in!");
    }

    public void linkListToFileList () {
        clientUnit.observableFilesList.addListener((ListChangeListener<String>) change -> {
            while (change.next()) {
                if (change.wasRemoved()) {
                    logTF.appendText("Deleted file: " + change);
                    filesListView.getItems().remove(change.getFrom());
                } else if (change.wasAdded()) {
                    logTF.appendText("New file: " + change);
                    System.out.println("added po cichu");
                    filesListView.getItems().add(change.toString());
                }
            }
        });

        filesListView.setCellFactory(param -> new ListCell<String>() {
            @Override
            public void updateItem(String filename, boolean empty) {
                super.updateItem(filename, empty);
                if (empty || (filename == null)) {
                    setText(null);
                    setGraphic(null);
                }
                else {
                    FontAwesomeIconView icon;
                    if (filename.contains(".txt"))
                        icon = new FontAwesomeIconView(FontAwesomeIcon.FILE_TEXT);
                    else if (filename.contains(".png|.jpg|.jpeg"))
                        icon = new FontAwesomeIconView(FontAwesomeIcon.FILE_PHOTO_ALT);
                    else
                        icon = new FontAwesomeIconView(FontAwesomeIcon.FILE);
                    setGraphic(icon);
                    setText(filename);
                }
            }
        });
    }
}
