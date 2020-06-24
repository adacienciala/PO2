import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTabPane;
import com.jfoenix.controls.JFXTreeView;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.scene.control.*;
import javafx.scene.paint.Paint;
import org.omg.PortableInterceptor.USER_EXCEPTION;

import java.io.IOException;
import java.io.File;
import java.util.ArrayList;

public class ServerSceneController {

    public TextArea logTA;
    public JFXListView<RegisteredUser> userListView;
    public JFXTreeView<File> serverFileTree;
    public JFXTabPane fileTabPane;

    private ServerUnit serverUnit;

    public void initialize() {
        try {
            serverUnit = new ServerUnit(5000, new File("").getAbsoluteFile(), logTA);
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
        linkFileListToMap();
        serverUnit.setupServerDir(new File("").getAbsoluteFile());
        serverUnit.awaitingThread.start();
        serverUnit.start();
    }

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
        for (RegisteredUser userStream : userListView.getItems())
        {
            if (userStream.getOut() != null)
            {
                System.out.println("Found online: " + userStream.getUsername());
                try {
                    userStream.getOut().writeObject(packet);
                } catch (IOException e) {
                    logTA.appendText(String.format("[ERROR] %s didn't get userlist", userStream.getUsername()));
                    System.out.println(String.format("[ERROR] %s didn't get userlist", userStream.getUsername()));
                }
            }
        }
    }

    public int findUserInList(RegisteredUser changedUser) {
        int i = 0;
        for (RegisteredUser user : userListView.getItems())
        {
            if (user.equals(changedUser)) return i;
            ++i;
        }
        return -1;
    }

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
//                        Tab onlineTab = new Tab(username);
//                        System.out.println(username + " tab name: " + onlineTab.getText());
//                        fileTabPane.getTabs().add(onlineTab);
                    }
                    else {
                        icon.setFill(Paint.valueOf("grey"));
//                        fileTabPane.getTabs().removeIf(tab ->
//                                tab.getText().equals(username));
                    }
                    setGraphic(icon);
                    setText(" " + user.getUsername());
                }
            }
        });
    }

    private int itemInTreeIndex (TreeItem<File> userDir, String filename) {
        int index = 0;
        for (TreeItem<File> file : userDir.getChildren())
        {
            if (file.getValue().getName().equals(filename))
                return index;
            else
                index++;
        }
        return -1;
    }

    public void linkFileListToMap () {
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
                    if (this.getTreeItem().equals(this.getTreeView().getRoot()))
                        return;
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
