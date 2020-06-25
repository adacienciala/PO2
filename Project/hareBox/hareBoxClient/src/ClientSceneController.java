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
import javafx.scene.control.*;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

import javafx.event.ActionEvent;

import java.io.File;
import java.io.IOException;

/**
 * <code>ClientSceneController</code> objects is responsible for the main GUI; looks specified in the fxml file. Initializes the {@link ClientUnit} object and is responsible for all the links between Observable Lists and GUI elements.
 * This object is created by {@link LoginSceneController}
 *
 * @author Adrianna Cieńciała
 * @version 1.0
 */
public class ClientSceneController {

    public JFXButton signOutBtn;
    public Label userDirLabel;
    public Label usernameLabel;
    public TextField logTF;
    public JFXListView<String> filesListView;
    public JFXListView<String[]> userListView;
    public JFXButton sendBtn;

    private String fileToShare;

    public File userDir;
    private ClientUnit clientUnit;

    /**
     * Closes the client and changes the scene to {@link ClientSceneController}.
     *
     * @param actionEvent Mouseclick on the button.
     * @throws IOException Thrown when there's problem with closing the outputstream or fxml can't be loaded.
     */
    public void signOut(ActionEvent actionEvent) throws IOException {
        clientUnit.outputStream.close();
        clientUnit.listeningThread.interrupt();

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

    /**
     * Creates the {@link ClientUnit} object and links the user and files' lists to the GUI components.
     * @param username
     * @param userPath
     */
    void initData(String username, String userPath) {
        this.userListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        this.userDir = new File(userPath);
        userDirLabel.setText(userPath);
        usernameLabel.setText(username);
        try {
            customCellFactoryList();
            clientUnit = new ClientUnit(5000, username, userDir, userListView, logTF);
            linkListToFileList();
            clientUnit.start();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        if (logTF.getText().equals(""))
            logTF.setText("Logged in!");
    }

    /**
     * Shares specific file with a range of selected users. Uses the {@link ClientUnit}'s method for sending files.
     * Variable user[0] holds the username, user[1] holds their status (online/offline).
     *
     * @param actionEvent Mouseclick on the button.
     */
    public void sendFiles(ActionEvent actionEvent) {
        File file = new File(String.valueOf(userDir.toPath().resolve(fileToShare)));
        for(String[] user : userListView.getSelectionModel().getSelectedItems()) {
            (new Thread(() -> {
                try {
                    clientUnit.sendFile(file, user[0], PacketObject.PACKET_TYPE.FILE_UPLOAD);
                } catch (IOException e) { System.out.println("Error while sharing with: " + user[0]); e.printStackTrace(); }
            })).start();
        }

        filesListView.setDisable(false);
        sendBtn.setVisible(false);
    }

    /**
     * Sets custom cell factory for the users' list, so it displays their status (online/offline).
     */
    public void customCellFactoryList () {
        userListView.setCellFactory(param -> new ListCell<String[]>() {
            @Override
            public void updateItem(String[] userInfo, boolean empty) {
                super.updateItem(userInfo, empty);
                if (empty || (userInfo == null)) {
                    setText(null);
                    setGraphic(null);
                }
                else {
                    String username = userInfo[0];
                    String status = userInfo[1];
                    FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.CIRCLE);
                    if (status.equals("online"))
                        icon.setFill(Paint.valueOf("green"));
                    else
                        icon.setFill(Paint.valueOf("grey"));
                    setGraphic(icon);
                    setText(" " + username);
                }
            }
        });
    }
    /**
     * Adds a listener to the observable list of files, so it's possible to display on <code>JFXListView</code>.
     * Sets the cell factory for <code>JFXListView</code>, so it displays the appropriate extension icon and adds a context menu for sharing.
     */
    public void linkListToFileList () {
        clientUnit.observableFilesList.addListener((ListChangeListener<String>) change -> {
            while (change.next()) {
                String filename;
                if (change.wasRemoved()) {
                    filename = filesListView.getItems().get(change.getFrom());
                    logTF.setText("Deleted file: " + filename);
                    filesListView.getItems().remove(change.getFrom());
                } else if (change.wasAdded()) {
                    filename = change.getList().get(change.getFrom());
                    logTF.setText("New file: " + filename);
                    filesListView.getItems().add(filename);
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
                    else if (filename.contains(".png") || filename.contains(".jpg") || filename.contains(".jpeg")) {
                        System.out.println("pic");
                        icon = new FontAwesomeIconView(FontAwesomeIcon.FILE_PHOTO_ALT);
                    }
                    else
                        icon = new FontAwesomeIconView(FontAwesomeIcon.FILE);
                    setGraphic(icon);
                    setText(filename);

                    ContextMenu contextMenu = new ContextMenu();
                    MenuItem shareItem = new MenuItem("Share");
                    contextMenu.getItems().add(shareItem);
                    setContextMenu(contextMenu);

                    shareItem.setOnAction((event) -> {
                        fileToShare = filename;
                        filesListView.setDisable(true);
                        sendBtn.setVisible(true);
                        System.out.println("Share " + fileToShare);
                    });
                }
            }
        });
    }
}