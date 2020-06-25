import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * <code>ServerUnit</code> object manages all communication between this server and its clients.
 * After standard files setup, this <code>Thread</code> constantly waits for new connections and looks for local files' changes.
 * This object is used by {@link ServerSceneController}
 *
 * @author Adrianna Cieńciała
 * @version 1.0
 */
public class ServerUnit extends Thread {

    /**
     * The directory of server's files
     */
    public File serverDir;

    /**
     * A map of registered users. Holds a user object as key and their status (online/offline)
     * It's linked with <code>JFXListView</code> in {@link ServerSceneController}
     *
     * @see RegisteredUser
     */
    public ObservableMap<RegisteredUser, Boolean> observableUsersMap;

    /**
     * A list of all the server's files. Maximum depth is 2 - user folder and then their files
     *
     * It's linked with <code>JFXTreeView</code> in {@link ServerSceneController}
     */
    public ObservableList<TreeItem<File>> observableFileList;

    private TextArea logTA;
    private ServerSocket serverSocket;
    public Thread awaitingThread;

    /**
     * Constructor of this object. Opens server's socket, creates users' map and files' list and starts the awaiting thread.
     * All parameters <strong>can't be null</strong>.
     *
     * @param port The number of local port. Can't be invalid.
     * @param logTA <code>TextArea</code> in {@link ServerSceneController} responsible for displaying logs.
     * @throws IOException Thrown when there are problems with server's socket
     */
    public ServerUnit(int port, TextArea logTA) throws IOException {
        this.logTA = logTA;
        serverSocket = new ServerSocket(port);
        observableUsersMap = FXCollections.observableMap(new ConcurrentHashMap<RegisteredUser, Boolean>());
        observableFileList = FXCollections.observableList(new CopyOnWriteArrayList<TreeItem<File>>());
        awaitingThread = new Thread(this::awaitClients);
    }

    /**
     * Goes over the servers directory and puts all registered users in the map.
     * The map is linked with a <code>JFXListView</code> of clients existing in {@link ServerSceneController}.
     *
     * @param workingDir The main directory of the server
     */
    public void setupServerDir(File workingDir) {
        logTA.appendText("[server] Setting up server's directory...\n");
            serverDir = new File(workingDir, "server_files");
            serverDir.mkdir();
        logTA.appendText("[server] Setting up registered users' directories...\n");
            File [] usersDirs = serverDir.listFiles();
            if (usersDirs != null && usersDirs.length > 0) {
                for (File userDir : usersDirs) {
                    observableUsersMap.put(new RegisteredUser(userDir.getName(), null), false);
                }
            }
    }

    /**
     * One of the two main functionalities of this object.
     * Waits for new connections. In case of such, gets the username from the client, starts a new individual {@link ClientThread} thread and registers the client as online by putting in a map.
     *
     * @see ClientThread
     */
    private void awaitClients() {
        logTA.appendText("[server] Waiting for clients...\n");
            while (true) {
                try {
                    Socket newClient = serverSocket.accept();
                    ObjectOutputStream outputStream = new ObjectOutputStream(newClient.getOutputStream());
                    ObjectInputStream inputStream = new ObjectInputStream(newClient.getInputStream());
                    String username = (String)inputStream.readObject();
                    File userDir = new File(serverDir, username);
                    (new ClientThread(userDir, inputStream, outputStream, logTA, observableUsersMap)).start();
                    Platform.runLater(() -> observableUsersMap.put(new RegisteredUser(username, outputStream), true));
                }
                catch (Exception ex) {
                    if (ex.getMessage().contains("\\w+")) {
                        logTA.appendText("[server]: Error while accepting clients!\n");
                    }
                    else {
                        String username = ex.getMessage();
                        observableUsersMap.replace(new RegisteredUser(username, null), false);
                    }
                    ex.printStackTrace();
                }
            }
    }

    /**
     * Scans the whole server every 1s, looking for changes (shares between users).
     */
    @Override
    public void run() {
        while (true) {
            for (File userDir : serverDir.listFiles()) {
                TreeItem<File> userBranch = new TreeItem<>(userDir);
                for (File userFile : userDir.listFiles())
                    userBranch.getChildren().add(new TreeItem<>(userFile));
                observableFileList.add(userBranch);
            }
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
