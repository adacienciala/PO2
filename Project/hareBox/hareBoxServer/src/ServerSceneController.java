import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTreeView;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.paint.Paint;
import org.omg.PortableInterceptor.USER_EXCEPTION;

import java.io.IOException;
import java.io.File;
import java.util.ArrayList;

public class ServerSceneController {

    public TextArea logTA;
    public JFXListView<RegisteredUser> userListView;
    public JFXTreeView<File> serverFileTree;

    private ServerUnit serverUnit;

    public void initialize() {
        try {
            serverUnit = new ServerUnit(5000, new File("").getAbsoluteFile(), logTA);
        }
        catch(IOException ex) {
            logTA.appendText("[ERROR] Couldn't open the server unit.");
            ex.printStackTrace();
        }
        TreeItem<File> root = new TreeItem<File>(new File("nice"));
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
                    System.out.println("[ERROR] " + userStream.getUsername() + "didn't get userlist");
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
                    if (serverUnit.observableUsersMap.get(user))
                        icon.setFill(Paint.valueOf("green"));
                    else
                        icon.setFill(Paint.valueOf("grey"));
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
                TreeItem<File> newFile = change.getList().get(change.getFrom());
                System.out.println("im in with " + newFile.getValue().getName());
                if (newFile.getValue().isDirectory() && (-1 == itemInTreeIndex(serverFileTree.getRoot(), newFile.getValue().getName()))) {
                    System.out.println("added dir " + newFile.getValue().getName());
                    serverFileTree.getRoot().getChildren().add(newFile);
                }
                else {
                    if (newFile.getValue().isDirectory()) {
                        System.out.println("hm");
                        continue;
                    }
                    int index = itemInTreeIndex(serverFileTree.getRoot(), newFile.getParent().getValue().getName());
                    TreeItem<File> oldUserDir = serverFileTree.getRoot().getChildren().get(index);
                    if (change.wasAdded()) {
                        System.out.println("added file " + newFile.getValue().getName());
                        if (-1 == itemInTreeIndex(oldUserDir, newFile.getValue().getName()))
                            oldUserDir.getChildren().add(newFile);
                        else {
                            System.out.println("-- with remove " + newFile.getValue().getName());
                            index = itemInTreeIndex(oldUserDir, newFile.getValue().getName());
                            oldUserDir.getChildren().remove(index);
                            oldUserDir.getChildren().add(newFile);
                        }
                    }
                    else if (change.wasRemoved()) {
                        System.out.println("removed file/dir " + newFile.getValue().getName());
                        index = itemInTreeIndex(oldUserDir, newFile.getValue().getName());
                        oldUserDir.getChildren().remove(index);
                        oldUserDir.getChildren().add(newFile);
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
                    System.out.println(" ---- " + filename);
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
