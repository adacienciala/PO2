import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.Timer;
import java.util.TimerTask;

public class NotificationServer
{
    private boolean isUp;
    private ServerSocket serverSocket;
    private ServerGUI GUI;
    private JTextArea logTA;

    private class clientThread implements Runnable
    {
        private Socket connection;
        private ObjectInputStream inputStream;
        private ObjectOutputStream outputStream;

        public clientThread(Socket connection)
        {
            logTA.append("-- > New client connected\n");
            this.connection = connection;
            try
            {
                this.inputStream = new ObjectInputStream(connection.getInputStream());
                this.outputStream = new ObjectOutputStream(connection.getOutputStream());
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public void run()
        {
            while (true)
            {
                try
                {
                    Message message = (Message) inputStream.readObject();
                    logTA.append("-- > read message\n");
                    new Timer().schedule(new TimerTask()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                outputStream.writeObject(message);
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }, message.getScheduledDate());
                }
                catch (Exception ex)
                {
                    logTA.append("-- > Client disconnected\n");
                    try
                    {
                        connection.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
    }

    public NotificationServer()
    {
        GUI = new ServerGUI("NotificationServer");
        GUI.setVisible(true);
        logTA = GUI.getLogTA();
        try
        {
            int port = Integer.parseInt(JOptionPane.showInputDialog("Enter port:"));
            serverSocket = new ServerSocket(port);
            logTA.append("SERVER UP\n");
            logTA.append("\tIP ADDRESS: " + InetAddress.getLocalHost().toString() + '\n');
            logTA.append("\tPORT: " + port + "\n-------------\n\n");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        isUp = true;
        while (isUp)
        {
            Socket clientSocket;
            try
            {
                clientSocket = serverSocket.accept();
                new Thread(new clientThread(clientSocket)).start();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        try
        {
            serverSocket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        new NotificationServer();
    }
}
