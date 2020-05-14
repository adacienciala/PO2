import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.rmi.server.ExportException;
import java.util.Calendar;
import java.util.Date;

public class MessageGUI extends JFrame
{
    public static final int WIDTH = 400;
    public static final int HEIGHT = 380;

    private JPanel mainPanel;
    private JPanel messagePanel;
    private JPanel infoPanel;
    private JPanel datePanel;
    private JSpinner clockSpinner;
    private JDateChooser JDateChooser1;
    private JButton setButton;
    private JPanel notiPanel;
    private JLabel logoLabel;
    private JTextArea notiTA;
    private JLabel notiLabel;


    private Socket socket;
    private ObjectOutputStream outputStream;

    public MessageGUI(Socket socket)
    {
        super();
        this.setMinimumSize(new Dimension(WIDTH, HEIGHT));
        this.setResizable(false);
        this.setIconImage(new ImageIcon(ConnectingGUI.class.getResource("logo.png")).getImage());
        this.setLocationRelativeTo(mainPanel);
        this.setContentPane(mainPanel);
        this.pack();
        this.socket = socket;
        try
        {
            this.outputStream = new ObjectOutputStream(socket.getOutputStream());
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        setButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String message = notiTA.getText();
                Date scheduledDate = JDateChooser1.getDate();
                Date scheduledTime = (Date) clockSpinner.getValue();
                scheduledDate.setHours(scheduledTime.getHours());
                scheduledDate.setMinutes(scheduledTime.getMinutes());
                scheduledDate.setSeconds(scheduledTime.getSeconds());
                try
                {
                    outputStream.writeObject(new Message(message, scheduledDate));
                }
                catch (IOException ex)
                {
                    ex.printStackTrace();
                }
                notiTA.setText("");
                JDateChooser1.setDate(new Date());
            }
        });
    }

    private void createUIComponents()
    {
        Date date = new Date();

        JDateChooser1 = new JDateChooser(date, "dd/MM/yyyy");
        JDateChooser1.setMinSelectableDate(date);

        SpinnerDateModel sm = new SpinnerDateModel(date, null, null, Calendar.HOUR_OF_DAY);
        clockSpinner = new JSpinner(sm);
        JSpinner.DateEditor de = new JSpinner.DateEditor(clockSpinner, "HH:mm:ss");
        clockSpinner.setEditor(de);
    }
}
