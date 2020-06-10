package hareBoxClient;

import javafx.scene.control.TextField;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientUnit {

    private Socket userSocket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private File userDir;

    public ClientUnit(int port, File userDir, TextField logTF) throws Exception {
        try {
            userSocket = new Socket(InetAddress.getLocalHost(), port);
            inputStream = new ObjectInputStream(userSocket.getInputStream());
            outputStream = new ObjectOutputStream(userSocket.getOutputStream());
            this.userDir = userDir;
        }
        catch (UnknownHostException ex) {
            logTF.setText("[ERROR] Unknown host.\n");
            throw new UnknownHostException("[ERROR] Unknown host.\n");
        }
        catch (IOException ex) {
            logTF.setText("[ERROR] Couldn't connect to server.\n");
            throw new IOException("[ERROR] Couldn't connect to server.\n");
        }
    }
}
