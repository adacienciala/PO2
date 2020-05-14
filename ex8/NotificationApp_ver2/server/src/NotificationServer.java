import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Timer;

public class NotificationServer
{
    private boolean isUp;
    private ServerSocket serverSocket;
    private ServerGUI GUI;
    private JTextArea logTA;

    private class ClientThread implements Runnable
    {
        private Socket connection;
        private ObjectInputStream inputStream;
        private ObjectOutputStream outputStream;
        private LinkedList<Message> messageQueue = new LinkedList<>();
        public final Object queueLock = new Object();
        private QueueManager queueManager;

        public class MessageSender extends Thread
        {
            Message messageToSend;
            long timeToWait;

            public MessageSender(Message messageToSend)
            {
                this.messageToSend = messageToSend;
            }

            public void run()
            {
                while(true)
                {
                    try
                    {
                        timeToWait = messageToSend.getScheduledDate().getTime() - new Date().getTime();
                        logTA.append("-- > sleeping\n");
                        sleep(timeToWait);
                        logTA.append("-- > woke up and sent!\n");
                        try
                        {
                            outputStream.writeObject(messageToSend);
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                        synchronized(queueLock)
                        {
                            messageQueue.remove(0);
                            messageToSend = messageQueue.get(0);
                            logTA.append("-- > took new message\n");
                        }
                    }
                    catch (Exception ex)
                    {
                        logTA.append("-- > message aborted\n");
                        return;
                    }
                }
            }
        }

        public class QueueManager extends Thread
        {
            Message currMessage;
            MessageSender messageSender;

            @Override
            public void run()
            {
                while (true)
                {
                    try
                    {

                        synchronized(queueLock)
                        {
                            queueLock.wait();
                            currMessage = messageQueue.get(0);
                        }
                        logTA.append("-- > message scheduled\n");
                        if (messageSender != null) messageSender.interrupt();
                        messageSender = new MessageSender(currMessage);
                        messageSender.start();
                    }
                    catch (Exception ex)
                    {
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

        public ClientThread(Socket connection)
        {
            logTA.append("-- > New client connected\n");
            this.connection = connection;
            this.queueManager = new QueueManager();
            this.queueManager.start();
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

        private void addMessage(Message message)
        {
            ListIterator<Message> itr = messageQueue.listIterator(0);
            while(true)
            {
                if (!itr.hasNext())
                {
                    itr.add(message);
                    return;
                }

                Message currMessage = (Message)itr.next();
                if (currMessage.compareTo(message) > 0)
                {
                    itr.previous();
                    itr.add(message);
                    return;
                }
            }
        }

        @Override
        public void run()
        {
            while (true)
            {
                try
                {
                    Message newMessage = (Message) inputStream.readObject();
                    synchronized(queueLock)
                    {
                        addMessage(newMessage);
                        queueLock.notifyAll();
                        logTA.append("-- > read message\n");
                    }
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
                        ex.printStackTrace();
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
                new Thread(new ClientThread(clientSocket)).start();
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
