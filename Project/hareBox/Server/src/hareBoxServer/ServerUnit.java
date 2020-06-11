package hareBoxServer;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.scene.control.TextArea;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ServerUnit extends Thread {

    TextArea logTA;
    public File serverDir;
    public ObservableMap<RegisteredUser, Boolean> observableUsersMap;

    private ServerSocket serverSocket;
    public Thread awaitingThread;

    public ServerUnit(int port, File workingDir, TextArea logTA) throws IOException{
        this.logTA = logTA;
        serverSocket = new ServerSocket(port);
        observableUsersMap = FXCollections.observableMap(new ConcurrentHashMap<RegisteredUser, Boolean>());
        awaitingThread = new Thread(this::awaitClients);
    }

    public void setupServerDir(File workingDir) {
        logTA.appendText("[server] Setting up server's directory...\n");
            serverDir = new File(workingDir, "server_files");
            serverDir.mkdir();
        logTA.appendText("[server] Setting up registered users' directories...\n");
            File [] usersDirs = serverDir.listFiles();
            if (usersDirs != null && usersDirs.length > 0)
            {
                for (File userDir : usersDirs)
                    observableUsersMap.put(new RegisteredUser(userDir.getName(), null), false);
            }
    }

    private void awaitClients() {
        logTA.appendText("[server] Waiting for clients...\n");
            while (true) {
                try {
                    Socket newClient = serverSocket.accept();
                    ObjectOutputStream outputStream = new ObjectOutputStream(newClient.getOutputStream());
                    ObjectInputStream inputStream = new ObjectInputStream(newClient.getInputStream());
                    String username = (String)inputStream.readObject();
                    File userDir = new File(serverDir, username);
                    Platform.runLater(new Runnable() {
                        public void run() {
                            observableUsersMap.putIfAbsent(new RegisteredUser(username, outputStream), true);
                        }
                    });
                    (new ClientThread(userDir, inputStream, outputStream)).start();
                    logTA.appendText("[server] " + username + " logged in.\n");
                }
                catch (Exception ex) {
                    if (ex.getMessage().contains("\\w+"))
                        logTA.appendText("[server]: Error while accepting clients!\n");
                    else
                    {
                        String username = ex.getMessage();
                        observableUsersMap.replace(new RegisteredUser(username, null), false);
                    }
                    ex.printStackTrace();
                }
            }
    }

    @Override
    public void run() {
        while (true) {
            //System.out.println("a");
        }
    }
}
