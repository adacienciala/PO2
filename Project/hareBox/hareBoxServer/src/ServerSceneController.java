import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTabPane;
import com.jfoenix.controls.JFXTreeView;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.scene.control.*;
import javafx.scene.paint.Paint;

import java.io.IOException;
import java.io.File;
import java.util.ArrayList;

/**
 * <code>ServerSceneController</code> objects is responsible for the GUI; looks specified in the fxml file. Initializes the {@link ServerUnit} object and is responsible for all the links between Observable Lists and GUI elements.
 * This object is created by {@link hareBox}
 *
 * @author Adrianna Cieńciała
 * @version 1.0
 */
public class ServerSceneController {

    public TextArea logTA;
    public JFXListView<RegisteredUser> userListView;
    public JFXTreeView<File> serverFileTree;
    public JFXTabPane fileTabPane;

    private ServerUnit serverUnit;

    /**
     * Sets up main GUI components - user's list and files' list. Creates a {@link ServerUnit} object.
     *
     * @see ServerUnit
     */
    public void initialize() {
        try {
            serverUnit = new ServerUnit(5000, logTA);
        }
        catch(IOException ex) {
            logTA.appendText("[ERROR] Couldn't open the server unit.");
            ex.printStackTrace();
        }
        TreeItem<File> root = new TreeItem<File>(new File("GOD"));
        root.setExpanded(true);
        serverFileTree.setRoot(root);
        serverFileTree.setShowRoot(false);

        linkUserListToMap();
        linkFileTreeToList();
        serverUnit.setupServerDir(new File("").getAbsoluteFile());
        serverUnit.awaitingThread.start();
        serverUnit.start();
    }

    /**
     * Gets the current users' list and sends it out to all online clients.
     *
     * @see PacketObject
     */
    public void updateClientsWithUsersList() {
        // make list of users
        ArrayList<String> userList = new ArrayList<>();
        for (RegisteredUser user : userListView.getItems()) {
            userList.add(user.getUsername() + " " + (user.isOnline ? "online" : "offline"));
        }
        PacketObject packet = new PacketObject(PacketObject.PACKET_TYPE.LIST_SYNCHRONIZE,
                null, null, null, null);
        packet.setUserList(userList);

        //send out to online users
        System.out.println("Start sending");
        for (RegisteredUser userStream : userListView.getItems()) {
            if (userStream.getOut() != null) {
                System.out.println("Found online: " + userStream.getUsername());
                try {
                    userStream.getOut().writeObject(packet);
                }
                catch (IOException e) {
                    logTA.appendText(String.format("[ERROR] %s didn't get userlist", userStream.getUsername()));
                    System.out.println(String.format("[ERROR] %s didn't get userlist", userStream.getUsername()));
                }
            }
        }
    }

    /**
     * Looks for the index of the specific user in the server's list, looking by their username.
     *
     * @param changedUser User to find in the list
     * @return Index of the user or -1, if they're not known to the server.
     */
    public int findUserInList(RegisteredUser changedUser) {
        int i = 0;
        for (RegisteredUser user : userListView.getItems()) {
            if (user.equals(changedUser)) return i;
            ++i;
        }
        return -1;
    }

    /**
     * Adds a listener to the observable map of users, so it's possible to display on <code>JFXListView</code>, if the user is online or offline.
     * Sets the cell factory for <code>JFXListView</code>, so it displays only the name and the appropriate status icon. Also, enables to add/remove a customized tab.
     *
     * <strong>It's impossible to add a tab to a new user, because they're not yet in the TreeList.</strong>
     */
    public void linkUserListToMap () {
        serverUnit.observableUsersMap.addListener((MapChangeListener<RegisteredUser, Boolean>) change -> {
            if (change.wasRemoved()) {
                logTA.appendText(String.format("[%s] went %s",
                        change.getKey().getUsername(),
                        change.getValueAdded() ? "ONLINE\n" : "OFFLINE\n"));

                change.getKey().isOnline = change.getValueAdded();
                int index = findUserInList(change.getKey());
                userListView.getItems().get(index).out = change.getKey().out;
                userListView.getItems().get(index).isOnline = change.getKey().isOnline;
                userListView.refresh();
                updateClientsWithUsersList();
            }
            else if (change.wasAdded()) {
                logTA.appendText(String.format("[server] New user: %s\n",
                        change.getKey().getUsername()));
                change.getKey().isOnline = change.getValueAdded();
                userListView.getItems().add(change.getKey());
                updateClientsWithUsersList();
            }
        });

        userListView.setCellFactory(param -> new ListCell<RegisteredUser>() {
            @Override
            public void updateItem(RegisteredUser user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || (user == null)) {
                    setText(null);
                    setGraphic(null);
                }
                else {
                    FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.CIRCLE);
                    String username = user.getUsername();
                    if (serverUnit.observableUsersMap.get(user)) {
                        icon.setFill(Paint.valueOf("green"));
                        Tab onlineTab = new Tab(username);
                        Platform.runLater(() -> {
                            int i = 0;
                            for (TreeItem<File> userDir : serverFileTree.getRoot().getChildren()) {
                                if (userDir.getValue().getName().equals(username)) {
                                    JFXListView<String> list = new JFXListView<>();
                                    list.getItems().addAll(userDir.getValue().list());
                                    linkListToTabList(userDir, list);
                                    onlineTab.setContent(list);
                                    break;
                                }
                                else ++i;
                            }
                            if (i < serverFileTree.getRoot().getChildren().size()) {
                                fileTabPane.getTabs().removeIf(tab -> tab.getText().equals(onlineTab.getText()));
                                fileTabPane.getTabs().add(onlineTab);
                            }
                            else { /* completely new user, can't give them a tab, cause they're not in the list and they won't be so deal with it */ }
                        });
                        System.out.println(fileTabPane.getTabs().size());
                    }
                    else {
                        icon.setFill(Paint.valueOf("grey"));
                        Platform.runLater(() -> fileTabPane.getTabs().removeIf(tab ->
                                tab.getText().equals(username)));
                    }
                    setGraphic(icon);
                    setText(" " + user.getUsername());
                }
            }
        });
    }

    /**
     * Adds a listener to the observable list of files, so it's possible to display on <code>JFXListView</code> in the user's tab without duplicates.
     * Sets the cell factory for <code>JFXListView</code>, so it displays only the filename and the appropriate extension icon.
     *
     * @param userDir User's branch in the <code>JFXTreeView</code> of files, which gives the observable list.
     * @param list List in the user's tab to connect to the branch.
     */
    private void linkListToTabList (TreeItem<File> userDir, JFXListView<String> list){
        userDir.getChildren().addListener((ListChangeListener<TreeItem<File>>) change -> {
            while (change.next()) {
                Platform.runLater(() -> {
                    if (change.wasAdded()) {
                        String newFilename = change.getList().get(change.getFrom()).getValue().getName();
                        list.getItems().add(newFilename);
                    }
                    else if (change.wasRemoved()) {
                        list.getItems().remove(list.getItems().get(change.getFrom()));
                    }
                });
            }
        });

        list.setCellFactory(param -> new ListCell<String>() {
            @Override
            public void updateItem(String filename, boolean empty) {
                super.updateItem(filename, empty);
                if (empty || (filename == null)) {
                    setText(null);
                    setGraphic(null);
                }
                else {
                    FontAwesomeIconView fileIcon;
                    if (filename.contains(".txt"))
                        fileIcon = new FontAwesomeIconView(FontAwesomeIcon.FILE_TEXT);
                    else if (filename.contains(".png") || filename.contains(".jpg") || filename.contains(".jpeg"))
                        fileIcon = new FontAwesomeIconView(FontAwesomeIcon.FILE_PHOTO_ALT);
                    else
                        fileIcon = new FontAwesomeIconView(FontAwesomeIcon.FILE);
                    setGraphic(fileIcon);
                    setText(filename);
                }
            }
        });
    }

    /**
     * Looks for the index of the specific user's directory in the server's list, looking by their username.
     *
     * @param userDir User's folder to find in the list
     * @return Index of the user's folder or -1, if they're not known to the server.
     */
    private int itemInTreeIndex (TreeItem<File> userDir, String filename) {
        int index = 0;
        for (TreeItem<File> file : userDir.getChildren()) {
            if (file.getValue().getName().equals(filename))
                return index;
            else index++;
        }
        return -1;
    }

    /**
     * Adds a listener to the observable list of files, so it's possible to display on <code>JFXTreeView</code> without duplicates.
     * Sets the cell factory for <code>JFXTreeView</code>, so it displays only the filename and the appropriate extension icon.
     */
    public void linkFileTreeToList() {
        serverUnit.observableFileList.addListener((ListChangeListener<TreeItem<File>>) change -> {
            while (change.next()) {
                TreeItem<File> newUserDir = change.getList().get(change.getFrom());
                if (-1 == itemInTreeIndex(serverFileTree.getRoot(), newUserDir.getValue().getName())) {
                    System.out.println("added dir " + newUserDir.getValue().getName());
                    serverFileTree.getRoot().getChildren().add(newUserDir);
                }
                else {
                    int index = itemInTreeIndex(serverFileTree.getRoot(), newUserDir.getValue().getName());
                    TreeItem<File> oldUserDir = serverFileTree.getRoot().getChildren().get(index);
                    if (change.wasAdded()) {
                        // remove file
                        oldUserDir.getChildren().removeIf ( oldItem ->
                                itemInTreeIndex(newUserDir, oldItem.getValue().getName()) == -1 );

                        // add file
                        for (TreeItem<File> file : newUserDir.getChildren()) {
                            if (itemInTreeIndex(oldUserDir, file.getValue().getName()) == -1) {
                                oldUserDir.getChildren().add(file);
                                System.out.println("added file " + newUserDir.getValue().getName());
                            }
                        }
                    }
                    else if (change.wasRemoved()) {
                        System.out.println("removed dir " + newUserDir.getValue().getName());
                        index = itemInTreeIndex(oldUserDir, newUserDir.getValue().getName());
                        oldUserDir.getChildren().remove(index);
                        oldUserDir.getChildren().add(newUserDir);
                    }
                }
            }
        });

        serverFileTree.setCellFactory(param -> new TreeCell<File>() {
            @Override
            public void updateItem(File userDir, boolean empty) {
                super.updateItem(userDir, empty);
                if (empty || (userDir == null)) {
                    setText(null);
                    setGraphic(null);
                }
                else {
                    if (this.getTreeItem().equals(this.getTreeView().getRoot())) return;
                    String filename = userDir.getName();
                    FontAwesomeIconView icon;
                    if (userDir.isDirectory())
                        icon = new FontAwesomeIconView(FontAwesomeIcon.FOLDER);
                    else {
                        if (filename.contains(".txt"))
                            icon = new FontAwesomeIconView(FontAwesomeIcon.FILE_TEXT);
                        else if (filename.contains(".png") || filename.contains(".jpg") || filename.contains(".jpeg"))
                            icon = new FontAwesomeIconView(FontAwesomeIcon.FILE_PHOTO_ALT);
                        else
                            icon = new FontAwesomeIconView(FontAwesomeIcon.FILE);
                    }
                    setGraphic(icon);
                    setText(filename);
                    this.getTreeItem().setExpanded(true);
                }
            }
        });
    }
}