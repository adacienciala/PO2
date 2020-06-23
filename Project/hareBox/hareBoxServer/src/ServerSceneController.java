import com.jfoenix.controls.JFXListView;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.MapChangeListener;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Paint;

import java.io.IOException;
import java.io.File;

public class ServerSceneController {

    public TextArea logTA;
    public JFXListView<RegisteredUser> userListView;

    private ServerUnit serverUnit;

    public void initialize() {
        try {
            serverUnit = new ServerUnit(5000, new File("").getAbsoluteFile(), logTA);
        }
        catch(IOException ex) {
            logTA.appendText("[ERROR] Couldn't open the server unit.");
            ex.printStackTrace();
        }
        linkListToMap();
        serverUnit.setupServerDir(new File("").getAbsoluteFile());
        serverUnit.awaitingThread.start();
        serverUnit.start();
    }

    public void linkListToMap () {
        serverUnit.observableUsersMap.addListener((MapChangeListener<RegisteredUser, Boolean>) change -> {
            if (change.wasRemoved()) {
                logTA.appendText(String.format("[%s] went %s",
                                                change.getKey().getUsername(),
                                                change.getValueAdded() ? "ONLINE\n" : "OFFLINE\n"));
                userListView.refresh();
            }
            else if (change.wasAdded()) {
                logTA.appendText(String.format("[server] New user: %s\n",
                                                change.getKey().getUsername()));
                userListView.getItems().add(change.getKey());
            }
        });

        userListView.setCellFactory(param -> new ListCell<RegisteredUser>() {
            @Override
            public void updateItem(RegisteredUser user, boolean empty) {
                super.updateItem(user, empty);
                if (user != null) {
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

}
