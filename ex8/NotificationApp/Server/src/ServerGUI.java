import javax.swing.*;
import java.awt.*;

public class ServerGUI extends JFrame
{
    public static final int WIDTH = 800;
    public static final int HEIGHT = 400;

    private JPanel mainPanel;
    private JTextArea logTA;

    public ServerGUI(String title)
    {
        super(title);
        this.setMinimumSize(new Dimension(WIDTH, HEIGHT));
        this.setResizable(false);
        this.setIconImage(new ImageIcon(ServerGUI.class.getResource("logo.png")).getImage());
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(mainPanel);
        this.pack();
    }

    public JTextArea getLogTA()
    {
        return logTA;
    }
}
