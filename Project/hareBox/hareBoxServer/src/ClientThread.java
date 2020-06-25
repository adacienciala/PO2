import javafx.application.Platform;
import javafx.collections.ObservableMap;
import javafx.scene.control.TextArea;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * <code>ClientThread</code> object manages all communication with a specific client.
 * Creates a <code>ListeningThread</code> Thread to get incoming files from the user and looks for changes in their local directory.
 * This object is created by {@link ServerUnit}.
 *
 * @author Adrianna Cieńciała
 * @version 1.0
 */

public class ClientThread extends Thread {

    private TextArea logTA;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private File userDir;
    private Thread listeningThread;
    private ObservableMap<RegisteredUser, Boolean> observableUsersMap;
    private File[] lastCheckedFiles;

    /**
     * <code>ListeningThread</code> object waits for incoming packets and uploads/deletes files from the user.
     * When client logs out, modifies the users' map, making them offline, and stops.
     *
     * @see PacketObject
     */
    private class ListeningThread extends Thread {

        @Override
        public void run() {
            while(true) {
                try {
                    PacketObject packet = (PacketObject)inputStream.readObject();
                    dealWithFile(packet);
                }
                catch (IOException | ClassNotFoundException e) {
                    Platform.runLater(() -> {
                        System.out.println("Logout " + userDir.getName());
                        observableUsersMap.replace(new RegisteredUser(userDir.getName(), null), false);
                    });
                    break;
                }
            }
        }

        /**
         * Checks, if the incoming packet calls for deletion or upload of a file, and does so.
         *
         * @param packet Acquired {@link PacketObject} object that's used for server-client communication.
         * @throws IOException Thrown when there's a problem with saving/deleting the file.
         */
        public void dealWithFile(PacketObject packet) throws IOException {
            switch (packet.getType())
            {
                case FILE_UPLOAD:
                {
                    Path filePath = userDir.getParentFile().toPath()
                            .resolve(packet.getRecipient())
                            .resolve(packet.getFileName());
                    OutputStream out = Files.newOutputStream(filePath);
                    out.write(packet.getData());
                    logTA.appendText(String.format("[%s] Saved file %s at %s\n",
                            userDir.getName(), filePath.getFileName(), filePath.getParent().getFileName()));
                    System.out.println(String.format("[%s] Saved file %s at %s\n",
                            userDir.getName(), filePath.getFileName(), filePath.getParent().getFileName()));
                    break;
                }
                case FILE_DELETE:
                {
                    Path filePath = userDir.toPath().resolve(packet.getFileName());
                    Files.delete(filePath);
                    logTA.appendText(String.format("[%s] Deleted file %s\n", userDir.getName(), filePath.getFileName()));
                    System.out.println(String.format("[%s] Deleted file %s\n", userDir.getName(), filePath.getFileName()));
                    break;
                }
            }
            lastCheckedFiles = userDir.listFiles();
        }
    }

    /**
     * Constructor of this object. Makes the client's folder on server (if doesn't exist), synchronizes local files with remote ones and starts the {@link ListeningThread} Thread.
     * All parameters <strong>can't be null</strong>.
     *
     * @param userDir The specific user's directory on the server. Carries user's name.
     * @param inputStream Opened input stream got from the socket.
     * @param outputStream Opened output stream got from the socket.
     * @param logTA TextArea used in {@link ServerSceneController} to display logs.
     * @param observableUsersMap Map of  the registered users. Holds a {@link RegisteredUser} object and their status (online/offline).
     * @throws IOException Thrown when the files synchronization couldn't be performed.
     */
    public ClientThread(File userDir, ObjectInputStream inputStream, ObjectOutputStream outputStream,
                        TextArea logTA, ObservableMap<RegisteredUser, Boolean> observableUsersMap) throws IOException {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.setName(userDir.getName());
        this.userDir = userDir;
        this.logTA = logTA;
        if (userDir.mkdir()) {
            logTA.appendText(String.format("[%s] Main directory created\n", userDir.getName()));
            System.out.println(String.format("[%s] Main directory created\n", userDir.getName()));
        }
        this.observableUsersMap = observableUsersMap;
        synchronizeWithClient();
        listeningThread = new ListeningThread();
        listeningThread.start();
    }

    /**
     * Sends a list of user's files on server and wait for the local version. Looks for missing files and sends them to the user.
     *
     * @throws IOException Thrown when user closed their streams.
     * @see PacketObject
     */
    private void synchronizeWithClient() throws IOException {
        try {
            sendFileList();
            PacketObject packet = (PacketObject) inputStream.readObject();
            List<String> userSide = Arrays.asList(packet.getFileList());
            File [] serverSide = userDir.listFiles();
            if (serverSide != null) {
                for (File file : serverSide) {
                    if (!userSide.contains(file.getName()))
                        sendFile(file, PacketObject.PACKET_TYPE.FILE_UPLOAD);
                }
            }
            lastCheckedFiles = userDir.listFiles();
        }
        catch (IOException e) {
            throw new IOException(userDir.getName());
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("Finished sending the list");
    }

    /**
     * Sends a special {@link PacketObject} message, containing only an array of files on server's side.
     *
     * @see PacketObject
     */
    private void sendFileList() {
        try {
            String [] myFiles = userDir.list();
            PacketObject packet = new PacketObject(PacketObject.PACKET_TYPE.FILE_SYNCHRONIZE,
                    null, myFiles, null, null);
            outputStream.writeObject(packet);
            logTA.appendText(String.format("[server] Sent file list to client %s\n", userDir.getName()));
            System.out.println(String.format("[server] Sent file list to client %s\n", userDir.getName()));
        }
        catch (IOException ex) {
            logTA.appendText(String.format("[server] Failed to sent file list to client %s\n", userDir.getName()));
            System.out.println(String.format("[server] Failed to sent file list to client %s\n", userDir.getName()));
        }
    }

    /**
     * Sends a {@link PacketObject} object, containing a file to send to user's local directory.
     *
     * @param file The file to be sent.
     * @param packetType Type of the message, that the packet brings.
     * @throws IOException Thrown when the object couldn't be send due to stream error. Contains a message to display in log in {@link ServerSceneController}.
     */
    private void sendFile(File file, PacketObject.PACKET_TYPE packetType) throws IOException {
        try {
            byte[] data = Files.readAllBytes(file.toPath());
            PacketObject packet = new PacketObject( packetType, this.getName(), null, file.getName(), data);
            outputStream.writeObject(packet);
            logTA.appendText(String.format("[server] Sent file %s to %s\n", file.getName(), userDir.getName()));
            System.out.println(String.format("[server] Sent file %s to %s\n", file.getName(), userDir.getName()));
        }
        catch (IOException ex) {
            throw new IOException(String.format("[%s] Error while sending file: %s\n", this.getName(), file.getName()));
        }
    }

    /**
     * Goes over a list of last known state of files in the directory and sends new files (shared from other users) to the client.
     *
     * @param current Current list of files in the user's folder on the server.
     */
    private void sendMissing(File [] current) {
        // any new files?
        for (File file : current) {
            if (!Arrays.asList(lastCheckedFiles).contains(file)) {
                System.out.println("Found to send: " + file.getName());
                try {
                    sendFile(file, PacketObject.PACKET_TYPE.FILE_UPLOAD);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Scans the user's folder on the server every 1s, looking for changes (shares between users). Sends out the new files.
     * Finishes when the {@link ListeningThread} Thread is interrupted (the user logged out).
     */
    @Override
    public void run() {
        try {
            Thread.sleep(2000);
            while (listeningThread.isAlive()) {
                sendMissing(userDir.listFiles());
                System.out.println("Alive " + userDir.getName());
                Thread.sleep(1000);
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}