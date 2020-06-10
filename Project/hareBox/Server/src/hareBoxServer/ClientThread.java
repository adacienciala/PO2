package hareBoxServer;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class ClientThread extends Thread {

    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private File userDir;

    public ClientThread(File userDir, ObjectInputStream inputStream, ObjectOutputStream outputStream) throws IOException {
        inputStream = inputStream;
        outputStream = outputStream;
        this.setName(userDir.getName());
        this.userDir = userDir;
        sendOfflineFiles();
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
            throw e;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void sendFile(File file, PacketObject.PACKET_TYPE packetType) throws IOException {
        try {
            byte[] data = Files.readAllBytes(file.toPath());
            PacketObject packet = new PacketObject( packetType, this.getName(), file.getName(), data);
            outputStream.writeObject(packet);

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
                e.printStackTrace();
            }
        }
    }
}
