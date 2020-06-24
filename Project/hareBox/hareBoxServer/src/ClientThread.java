import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ClientThread extends Thread {

    private TextArea logTA;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private File userDir;
    private Thread listeningThread;
    private ObservableMap<RegisteredUser, Boolean> observableUsersMap;
    private File[] lastCheckedFiles;
    private class ListeningThread extends Thread {

        @Override
        public void run() {
            while(!this.isInterrupted()) {
                try {
                    System.out.println("Nasluchuje plikow");
                    PacketObject packet = (PacketObject)inputStream.readObject();
                    System.out.println("Got sth");
                    (new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                dealWithFile(packet);
                            } catch (IOException e) { e.printStackTrace(); }
                        }
                    })).start();
                } catch (IOException | ClassNotFoundException e) {
                    Platform.runLater(new Runnable() {
                        public void run() {
                            observableUsersMap.replace(new RegisteredUser(userDir.getName(), null), false);
                        }
                    });
                    break;
                }
            }
        }

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

    public ClientThread(File userDir, ObjectInputStream inputStream, ObjectOutputStream outputStream,
                        TextArea logTA, ObservableMap<RegisteredUser, Boolean> observableUsersMap) throws IOException {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.setName(userDir.getName());
        this.userDir = userDir;
        this.logTA = logTA;
        if (userDir.mkdir())
        {
            logTA.appendText(String.format("[%s] Main directory created\n", userDir.getName()));
            System.out.println(String.format("[%s] Main directory created\n", userDir.getName()));
        }
        this.observableUsersMap = observableUsersMap;
        synchronizeWithClient();
        listeningThread = new ListeningThread();
        listeningThread.start();
    }

    private void synchronizeWithClient() throws IOException {
        try {
            String [] myFiles = userDir.list();
            sendFileList();
            System.out.println("Czekam na liste");
            PacketObject packet = (PacketObject) inputStream.readObject();
            System.out.println("Got lista");
            List<String> userSide = Arrays.asList(packet.getFileList());
            File [] serverSide = userDir.listFiles();
            if (serverSide != null)
            {
                for (File file : serverSide)
                {
                    if (!userSide.contains(file.getName()))
                        sendFile(file, PacketObject.PACKET_TYPE.FILE_UPLOAD);
                }
            }
            lastCheckedFiles = userDir.listFiles();
        } catch (IOException e) {
            throw new IOException(userDir.getName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("Finished sending");
    }

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

    private synchronized void sendFile(File file, PacketObject.PACKET_TYPE packetType) throws IOException {
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

    private void sendMissing(File [] current) {
        // any new files?
        for (File file : current)
        {
            if (!Arrays.asList(lastCheckedFiles).contains(file)) {
                System.out.println("Found to send: " + file.getName());
                (new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            sendFile(file, PacketObject.PACKET_TYPE.FILE_UPLOAD);
                        } catch (IOException e) { e.printStackTrace(); }
                    }
                })).start();
            }
        }
    }

    @Override
    public void run() {
        try {
            Thread.sleep(2000);
            while (true) {
                sendMissing(userDir.listFiles());
                System.out.println("Checked");
                Thread.sleep(3000);
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
