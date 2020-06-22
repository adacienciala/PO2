package hareBoxClient;

import javafx.scene.control.TextField;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class ClientUnit extends Thread {

    private final TextField logTF;
    private Socket userSocket;
    private ObjectInputStream inputStream;
    public ObjectOutputStream outputStream;
    private File userDir;
    private String username;
    private Thread listeningThread;
    private File[] filesList;

    public class ListeningThread extends Thread {

        @Override
        public void run() {
            PacketObject packet;
            while(true) {
                try {
                    System.out.println("Tried reading ");
                    packet = (PacketObject)inputStream.readObject();
                    System.out.println("Finished reading");
                    Path filePath = userDir.getParentFile().toPath()
                                            .resolve(packet.getRecipient())
                                            .resolve(packet.getFileName());
                    OutputStream out = Files.newOutputStream(filePath);
                    out.write(packet.getData());
                    logTF.setText("Saved file: " + packet.getFileName());
                    System.out.println("Saved file: " + packet.getFileName());
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

    public ClientUnit(int port, String username, File userDir, TextField logTF) throws Exception {
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
        this.logTF = logTF;
        logTF.setText("Sent name");
        receiveOfflineFiles();
        sendOfflineFiles();
        filesList = userDir.listFiles();
    }

    private void receiveOfflineFiles() {
        listeningThread = new ListeningThread();
        listeningThread.start();
        File [] myFiles = userDir.listFiles();
        try {
            outputStream.writeObject(myFiles);
        } catch (IOException e) {
            logTF.setText("Failed to sent offline files to server");
        }
        logTF.setText("Sent list of offline files to server");
    }

    private void sendOfflineFiles() throws IOException {
        try {
            List<File> serverSide = Arrays.asList((File[])inputStream.readObject());
            File [] userSide = userDir.listFiles();
            if (userSide != null)
            {
                for (File file : userSide)
                {
                    if (!serverSide.contains(file))
                        sendFile(file, this.username, PacketObject.PACKET_TYPE.FILE_UPLOAD);
                }
            }
        } catch (IOException e) {
            throw new IOException(userDir.getName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("Finished sending");
    }

    private void sendFile(File file, String recipentName, PacketObject.PACKET_TYPE packetType) throws IOException {
        try {
            byte[] data = Files.readAllBytes(file.toPath());
            PacketObject packet = new PacketObject(packetType, recipentName, file.getName(), data);
            outputStream.writeObject(packet);
            System.out.println("Sent: " + file.getName() + " to: " + this.userDir.getName());
        }
        catch (IOException ex) {
            throw new IOException(String.format("Error while sending file: %s\n", file.getName()));
        }
    }

    @Override
    public void run() {
        while (true) {
            File [] filesList = userDir.listFiles();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
