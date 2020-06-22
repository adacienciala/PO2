package hareBoxServer;

import javafx.application.Platform;
import javafx.collections.ObservableMap;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class ClientThread extends Thread {

    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private File userDir;
    private ObservableMap<RegisteredUser, Boolean> observableUsersMap;

    public ClientThread(File userDir, ObjectInputStream inputStream, ObjectOutputStream outputStream,
                        ObservableMap<RegisteredUser, Boolean> observableUsersMap) throws IOException {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.setName(userDir.getName());
        this.userDir = userDir;
        this.observableUsersMap = observableUsersMap;
        if (userDir.mkdir()) System.out.println("created " + userDir);
        sendOfflineFiles();
        receiveOfflineFiles();
    }

    private void receiveOfflineFiles() {
        File [] myFiles = userDir.listFiles();
        try {
            outputStream.writeObject(myFiles);
        } catch (IOException e) {
            System.out.println("Failed to sent offline files to: " + userDir.getName());
        }
        System.out.println("Sent offline files to: " + userDir.getName());
    }

    private void sendOfflineFiles() throws IOException {
        try {
            List<File> userSide = Arrays.asList((File[])inputStream.readObject());
            File [] serverSide = userDir.listFiles();
            if (serverSide != null)
            {
                for (File file : serverSide)
                {
                    if (!userSide.contains(file))
                        sendFile(file, PacketObject.PACKET_TYPE.FILE_UPLOAD);
                }
            }
        } catch (IOException e) {
            throw new IOException(userDir.getName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("Finished sending");
    }

    private void sendFile(File file, PacketObject.PACKET_TYPE packetType) throws IOException {
        try {
            byte[] data = Files.readAllBytes(file.toPath());
            PacketObject packet = new PacketObject( packetType, this.getName(), file.getName(), data);
            System.out.println(packet.getClass().toString());
            outputStream.writeObject(packet);
            System.out.println("[server] Sent: " + file.getName() + " to: " + this.userDir.getName());
        }
        catch (IOException ex) {
            throw new IOException(String.format("[%s] Error while sending file: %s\n", this.getName(), file.getName()));
        }
    }

    @Override
    public void run() {
        while(true) {
            try {
                PacketObject packet = (PacketObject)inputStream.readObject();
                switch (packet.getType())
                {
                    case FILE_UPLOAD:
                    {
                        Path filePath = userDir.getParentFile().toPath()
                                                                .resolve(packet.getRecipient())
                                                                .resolve(packet.getFileName());
                        OutputStream out = Files.newOutputStream(filePath);
                        out.write(packet.getData());
                        // signal server
                    }
                        break;
                    case FILE_DELETE:
                    {
                        Path filePath = userDir.toPath().resolve(packet.getFileName());
                        Files.delete(filePath);
                    }
                        break;
                }
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
}
