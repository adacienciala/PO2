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
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientUnit extends Thread {

    private final TextField logTF;
    private Socket userSocket;
    private ObjectInputStream inputStream;
    public ObjectOutputStream outputStream;
    private File userDir;
    private String username;
    public Thread listeningThread;
    public JFXListView<String[]> userListView;
    public ObservableList<String> observableFilesList;

    private class ListeningThread extends Thread {

        @Override
        public void run() {
            PacketObject packet;
            while(true) {
                try {
                    System.out.println("[LT] Nasluchuje plikow " + this.getId());
                    packet = (PacketObject)inputStream.readObject();
                    System.out.println("[LT] Odebralam plik");
                    if (packet.getType() == PacketObject.PACKET_TYPE.LIST_SYNCHRONIZE) {
                        System.out.println("Got userList");
                        userListView.getItems().clear();
                        userListView.refresh();
                        ArrayList<String[]> userList = new ArrayList<>();
                        for (String user : packet.getUserList())
                        {
                            String username = user.substring(0);
                            String status = user.substring(user.indexOf(" ") + 1);
                            userList.add(new String[]{username, status});
                        }
                        Platform.runLater(new Runnable() {
                            public void run() {
                                userListView.getItems().addAll(userList);
                            }
                        });
                    }
                    else {
                        Path filePath = userDir.toPath().resolve(packet.getFileName());
                        System.out.println(filePath);
                        OutputStream out = Files.newOutputStream(filePath);
                        out.write(packet.getData());
                        logTF.setText("Saved file: " + packet.getFileName());
                        System.out.println("Saved file: " + packet.getFileName());
                    }
                }
                catch (IOException e) {
                    logTF.setText("[ERROR] Couldn't connect to server.");
                    System.out.println("Couldn't connect to server.");
                }
                catch (ClassNotFoundException e) {
                    logTF.setText("[ERROR] Received package is weird.");
                    System.out.println("[ERROR] Received package is weird.");
                }
            }
        }
    }

    public ClientUnit(int port, String username, File userDir, JFXListView<String[]> userListView, TextField logTF) throws Exception {
        try {
            userSocket = new Socket(InetAddress.getLocalHost(), port);
            inputStream = new ObjectInputStream(userSocket.getInputStream());
            outputStream = new ObjectOutputStream(userSocket.getOutputStream());
        }
        catch (UnknownHostException ex) {
            logTF.setText("[ERROR] Unknown host.");
            throw new UnknownHostException("[ERROR] Unknown host.");
        }
        catch (IOException ex) {
            logTF.setText("[ERROR] Couldn't connect to server.");
            throw new IOException("[ERROR] Couldn't connect to server.");
        }
        this.userDir = userDir;
        this.username = username;
        outputStream.writeObject(username);
        this.observableFilesList = FXCollections.observableArrayList(new CopyOnWriteArrayList<>());
        this.userListView = userListView;
        this.logTF = logTF;
        synchronizeWithServer();
        listeningThread = new ListeningThread();
        listeningThread.start();
    }

    private void synchronizeWithServer() throws IOException {
        try {
            String [] myFiles = userDir.list();
            sendFileList();
            PacketObject packet = (PacketObject) inputStream.readObject();
            List<String> serverSide = Arrays.asList(packet.getFileList());
            File [] userSide = userDir.listFiles();
            if (userSide != null)
            {
                for (File file : userSide)
                {
                    if (!serverSide.contains(file.getName()))
                        sendFile(file, this.username, PacketObject.PACKET_TYPE.FILE_UPLOAD);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException(userDir.getName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        logTF.setText("Synchronized with server");
        System.out.println("Synchronized with server");
    }

    private void sendFileList() {
        try {
            String [] myFiles = userDir.list();
            PacketObject packet = new PacketObject(PacketObject.PACKET_TYPE.FILE_SYNCHRONIZE,
                                            this.username, myFiles, null, null);
            outputStream.writeObject(packet);
            System.out.println("Sent list of files to server");
        }
        catch (IOException ex) {
            System.out.println("Failed to sent file list to server");
        }
    }

    private void sendFile(File file, String recipentName, PacketObject.PACKET_TYPE packetType) throws IOException {
        try {
            byte[] data = null;
            if (packetType == PacketObject.PACKET_TYPE.FILE_UPLOAD)
                data = Files.readAllBytes(file.toPath());
            PacketObject packet = new PacketObject(packetType, recipentName, null, file.getName(), data);
            outputStream.writeObject(packet);
            System.out.println("Sent: " + file.getName() + " to: " + this.userDir.getName());
        }
        catch (IOException ex) {
            throw new IOException(String.format("Error while sending file: %s\n", file.getName()));
        }
    }

    private void sendMissing(File [] current) {
        // any new files?
        for (File file : current)
        {
            if (!observableFilesList.contains(file.getName())) {
                try {
                    System.out.println("Found to send: " + file.getName());
                    sendFile(file, this.username, PacketObject.PACKET_TYPE.FILE_UPLOAD);
                }
                catch (IOException e) {
                    System.out.println("FUCKED UP DURING SENDING MISSING FILE");
                }
                Platform.runLater(new Runnable() {
                    public void run() {
                        observableFilesList.add(file.getName());
                    }
                });
            }
        }

        // any files deleted?
        List<String> toDelete = new ArrayList<>();
        List<String> currentList = new ArrayList<>(Collections.emptyList());
        for (File file : current)
            currentList.add(file.getName());
        for (String filename : observableFilesList)
        {
            if (!currentList.contains(filename)) {
                try {
                    sendFile(new File(filename), this.username, PacketObject.PACKET_TYPE.FILE_DELETE);
                    System.out.println("Found to delete: " + filename);
                }
                catch (IOException e) {
                    System.out.println("Error while sending missing file");
                }
                toDelete.add(filename);
            }
        }
        for (String filename : toDelete)
        {
            Platform.runLater(new Runnable() {
                public void run() {
                    observableFilesList.remove(filename);
                }
            });
        }
    }


    @Override
    public void run() {
        try {
            Thread.sleep(1000);
            for (String filename : userDir.list())
                observableFilesList.add(filename);
            while (true) {
                sendMissing(userDir.listFiles());
                System.out.println("Checked");
                Thread.sleep(1000);
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
