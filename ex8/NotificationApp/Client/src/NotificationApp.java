import javax.swing.*;

public class NotificationApp
{

    private JFrame GUI;
    private String notiText;


    public NotificationApp()
    {
        SwingUtilities.invokeLater(new Runnable() 
        {
            public void run() 
            {
                GUI = new ConnectingGUI("Notification App");
                GUI.setVisible(true);
            }
        });
    }

    public static void main(String[] args)
    {
        NotificationApp app = new NotificationApp();
    }
}