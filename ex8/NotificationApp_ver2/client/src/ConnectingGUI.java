import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.*;

public class ConnectingGUI extends JFrame
{

    public static final int WIDTH = 400;
    public static final int HEIGHT = 400;
    private JPanel mainPanel;
    private JPanel connectingPanel;
    private JPanel infoPanel;
    private JLabel ipLabel;
    private JTextField ipTF;
    private JLabel portLabel;
    private JTextField portTF;
    private JButton connectButton;
    private JPanel logoPanel;
    private JLabel logoLabel;
    private JTextArea notiTA;

    private class IncomingsThread implements Runnable
    {
        private ObjectInputStream inputStream;

        public IncomingsThread(Socket socket)
        {
            try
            {
                this.inputStream = new ObjectInputStream(socket.getInputStream());
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        @Override
        public void run()
        {
            Message incomingMessage;
            while (true)
            {
                try
                {
                    incomingMessage = (Message)(inputStream.readObject());
                    JOptionPane.showMessageDialog(mainPanel, incomingMessage.getMessage());
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        }
    }

    public ConnectingGUI(String title)
    {
        super(title);
        this.setMinimumSize(new Dimension(WIDTH, HEIGHT));
        this.setResizable(false);
        this.setIconImage(new ImageIcon(ConnectingGUI.class.getResource("logo.png")).getImage());
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(mainPanel);
        this.pack();
        connectButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    Socket socket = new Socket(ipTF.getText(), Integer.parseInt(portTF.getText()));
                    new MessageGUI(socket).setVisible(true);
                    new Thread(new IncomingsThread(socket)).start();
                }
                catch (UnknownHostException exc)
                {
                    JOptionPane.showMessageDialog(mainPanel, "Unknown host!");
                }
                catch (Exception ex)
                {
                    JOptionPane.showMessageDialog(mainPanel, "Incorrect input data!");
                }
            }
        });
    }
}
