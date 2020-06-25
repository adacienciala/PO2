import com.jfoenix.controls.JFXListView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <code>ServerUnit</code> object manages all communication between this client and the server.
 * After standard files and lists setup, this <code>Thread</code> constantly listens to new packets (in {@link ListeningThread}) and looks for local files' changes.
 * This object is used by {@link ClientSceneController}
 *
 * @author Adrianna Cieńciała
 * @version 1.0
 */
public class ClientUnit extends Thread {

    /**
     * Lock to the OutputObjectStream, so sending a file can be delegated.
     */
    public ReentrantLock lockOut = new ReentrantLock();

    /**
     * Lock to the TextField displaying logs.
     */
    public ReentrantLock lockLog = new ReentrantLock();

    private final TextField logTF;
    private Socket userSocket;
    private ObjectInputStream inputStream;
    public ObjectOutputStream outputStream;
    private File userDir;
    private String username;
    public Thread listeningThread;

    /**
     * A list of registered users from the server. Holds the username (index 0) and status (online/offline; index [1])
     * It's linked with <code>JFXListView</code> in {@link ClientSceneController}
     */
    public JFXListView<String[]> userListView;

    /**
     * A list of all the user's files, by filenames. No folders.
     *
     * It's linked with <code>JFXListView</code> in {@link ClientSceneController}
     */
    public ObservableList<String> observableFilesList;

    /**
     * <code>ListeningThread</code> object waits for incoming packets and uploads files from the server or updates the users' list.
     * When the server disconnects, the Thread dies and the {@link ClientUnit} knows it should die, too.
     *
     * @see PacketObject
     */
    private class ListeningThread extends Thread {

        @Override
        public void run() {
            PacketObject packet;
            while(!this.isInterrupted()) {
                try {
                    packet = (PacketObject)inputStream.readObject();
                    if (packet.getType() == PacketObject.PACKET_TYPE.LIST_SYNCHRONIZE) {
                        System.out.println("Got userList");
                        userListView.getItems().clear();
                        userListView.refresh();
                        ArrayList<String[]> userList = new ArrayList<>();
                        for (String user : packet.getUserList()) {
                            String username = user.substring(0, user.indexOf(" "));
                            String status = user.substring(user.indexOf(" ") + 1);
                            userList.add(new String[]{username, status});
                        }
                        Platform.runLater(() -> userListView.getItems().addAll(userList));
                    }
                    else {
                        PacketObject temp = packet;
                        (new Thread(() -> {
                            try {
                                Path filePath = userDir.toPath().resolve(temp.getFileName());
                                System.out.println(filePath);
                                OutputStream out = Files.newOutputStream(filePath);
                                out.write(temp.getData());
                                lockLog.lock();
                                logTF.setText("Saved file " + temp.getFileName());
                                lockLog.unlock();
                                System.out.println("Saved file " + temp.getFileName());
                            } catch (IOException e) { e.printStackTrace(); }
                        })).start();
                    }
                }
                catch (IOException e) {
                    logTF.setText("[ERROR] Couldn't connect to server.");
                    System.out.println("[ERROR] Couldn't connect to server.");
                    break;
                }
                catch (ClassNotFoundException e) {
                    logTF.setText("[ERROR] Received package is weird.");
                    System.out.println("[ERROR] Received package is weird.");
                    break;
                }
            }
        }
    }

    /**
     * Constructor of this object. Created the users' list, identifies to the server, synchronizes files and starts the {@link ListeningThread} Thread.
     * All parameters <strong>can't be null</strong>.
     *
     * @param port The number of local port. Can't be invalid.
     * @param username User's name, by which they're identified by the server.
     * @param userDir User's chosen directory.
     * @param userListView List of the registered users from server.
     * @param logTF TextField used in {@link ClientSceneController} to display logs.
     * @throws Exception Thrown when there are connectivity issues.
     */
    public ClientUnit(int port, String username, File userDir, JFXListView<String[]> userListView, TextField logTF) throws Exception {
        try {
            userSocket = new Socket(InetAddress.getLocalHost(), port);
            inputStream = new ObjectInputStream(userSocket.getInputStream());
            outputStream = new ObjectOutputStream(userSocket.getOutputStream());
        }
        catch (UnknownHostException ex) {
            logTF.setText("[ERROR] Unknown host");
            throw new UnknownHostException("[ERROR] Unknown host");
        }
        catch (IOException ex) {
            logTF.setText("[ERROR] Couldn't connect to server");
            throw new IOException("[ERROR] Couldn't connect to server");
        }
        this.userDir = userDir;
        this.username = username;
        this.observableFilesList = FXCollections.observableArrayList(new CopyOnWriteArrayList<>());
        this.userListView = userListView;
        this.logTF = logTF;
        outputStream.writeObject(username);
        synchronizeWithServer();
        listeningThread = new ListeningThread();
        listeningThread.start();
    }

    /**
     * Sends a list of user's local files to server and wait for the remote version. Looks for missing files and sends them to the server.
     *
     * @throws IOException Thrown when server closed the streams.
     * @see PacketObject
     */
    private void synchronizeWithServer() throws IOException {
        try {
            sendFileList();
            PacketObject packet = (PacketObject) inputStream.readObject();
            List<String> serverSide = Arrays.asList(packet.getFileList());
            File [] userSide = userDir.listFiles();
            if (userSide != null) {
                for (File file : userSide) {
                    if (!serverSide.contains(file.getName()))
                        (new Thread(() -> {
                            try {
                                sendFile(file, username, PacketObject.PACKET_TYPE.FILE_UPLOAD);
                            } catch (IOException e) { e.printStackTrace(); }
                        })).start();
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new IOException(userDir.getName());
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        logTF.setText("Synchronized with server");
        System.out.println("Synchronized with server");
    }

    /**
     * Sends a special {@link PacketObject} message, containing only an array of files on client's side.
     *
     * @see PacketObject
     */
    private void sendFileList() {
        try {
            String [] myFiles = userDir.list();
            PacketObject packet = new PacketObject(PacketObject.PACKET_TYPE.FILE_SYNCHRONIZE,
                    this.username, myFiles, null, null);
            outputStream.writeObject(packet);
            logTF.setText("Sent list of files to server");
            System.out.println("Sent list of files to server");
        }
        catch (IOException ex) {
            logTF.setText("[ERROR] Failed to sent file list to server");
            System.out.println("[ERROR] Failed to sent file list to server");
        }
    }

    /**
     * Sends a {@link PacketObject} object, containing a file to send to user's remote directory.
     *
     * @param file The file to be sent.
     * @param recipientName The user that the packet affects.
     * @param packetType Type of the message, that the packet brings.
     * @throws IOException Thrown when the object couldn't be send due to stream error. Contains a message to display in log in {@link ClientSceneController}.
     */
    public void sendFile(File file, String recipientName, PacketObject.PACKET_TYPE packetType) throws IOException {
        try {
            byte[] data = null;
            if (packetType == PacketObject.PACKET_TYPE.FILE_UPLOAD)
                data = Files.readAllBytes(file.toPath());
            PacketObject packet = new PacketObject(packetType, recipientName, null, file.getName(), data);
            lockOut.lock();
            outputStream.writeObject(packet);
            lockOut.unlock();
            lockLog.lock();
            logTF.setText(String.format("Sent %s to %s\n", file.getName(), recipientName));
            lockLog.unlock();
            System.out.println(String.format("Sent %s to %s", file.getName(), recipientName));
        }
        catch (IOException ex) {
            lockOut.unlock();
            ex.printStackTrace();
            logTF.setText(String.format("[ERROR] Couldn't send file: %s", file.getName()));
            throw new IOException(String.format("[ERROR] Couldn't send file: %s", file.getName()));
        }
    }

    /**
     * Goes over a observable list of files in the directory and sends a packet with files to upload/delete to the server.
     *
     * @param current Current list of files in the user's folder.
     */
    private void sendMissing(File [] current) {
        // any new files?
        for (File file : current) {
            if (!observableFilesList.contains(file.getName())) {
                System.out.println("Found to send " + file.getName());
                (new Thread(() -> {
                    try {
                        sendFile(file, username, PacketObject.PACKET_TYPE.FILE_UPLOAD);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                })).start();
                Platform.runLater(() -> observableFilesList.add(file.getName()));
            }
        }

        // any files deleted?
        List<String> toDelete = new ArrayList<>();
        List<String> currentList = new ArrayList<>(Collections.emptyList());
        for (File file : current)
            currentList.add(file.getName());
        for (String filename : observableFilesList) {
            if (!currentList.contains(filename)) {
                (new Thread(() -> {
                    try {
                        sendFile(new File(filename), username, PacketObject.PACKET_TYPE.FILE_DELETE);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                })).start();
                System.out.println("Found to delete: " + filename);
                toDelete.add(filename);
            }
        }
        for (String filename : toDelete) {
            Platform.runLater(() -> observableFilesList.remove(filename));
        }
    }

    /**
     * Scans the user's folder every 1s, looking for changes. Sends the new files to server.
     * Finishes when the {@link ListeningThread} Thread is dead (the user logged out).
     */
    @Override
    public void run() {
        try {
            Thread.sleep(1000);
            for (String filename : userDir.list())
                observableFilesList.add(filename);

            while (listeningThread.isAlive()) {
                sendMissing(userDir.listFiles());
                System.out.println("Alive");
                Thread.sleep(1000);
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}