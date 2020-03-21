import java.util.*;
import java.io.*;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFrame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// I mean it started with looking fine
// and then it just.. escalated... I'm sorry
public class textFileLinePicker 
{
    private static String path;

    public static void main(String[] args) 
    {
        // get the file's path
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the file path, please:");
        path = sc.nextLine();

        // open the file, save the path
        File srcFile = new File(path);
        if (!srcFile.canRead()) 
        {
            System.out.println("Give me a proper path next time.");
            System.exit(1);
        }
        savePathToFile(srcFile);

        // choose between GUI or console
        System.out.println("GUI or console way? [G/C]");
        char ans = sc.next().charAt(0);
        switch(ans)
        {
        case 'C':
        case 'c':
            printingLoop(srcFile);
            break;
        default:
            new simpleGUICauseItsEasierThanPrintingStuffInOneLineLol(srcFile);
        }
    }

    private static void savePathToFile(File srcFile) 
    {
        // did the user give us absolute path? or not?
        String pathParent = srcFile.getParent() == null ? System.getProperty("user.dir") : srcFile.getParent();

        File pathFile = new File(pathParent + "\\given_path.txt");
        try 
        {
            BufferedWriter pathBuffer = new BufferedWriter(new FileWriter(pathFile));
            pathBuffer.write(pathParent + "\\" + srcFile.getName());
            pathBuffer.close();
        } 
        catch (IOException ex) 
        {
            System.out.println("Couldn't save the path to file, lol, why did you want me to, anyway?");
            System.exit(1);
        }
    }

    private static void printingLoop(File srcFile) 
    {
        Scanner sc = new Scanner(System.in);
        StringBuilder readLine = new StringBuilder("");

        // clear the console and print the prompt
        try 
        {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            System.out.println("Click enter to get new lines!");
        }
        catch (Exception e) {/* to be frank, I don't care at this point anymore*/}

        try 
        {
            BufferedReader buffer = new BufferedReader(new FileReader(srcFile));
            while (true) 
            {
                // clear the console, input won't show up
                Console c = System.console();
                char[] getCharXD = c.readPassword();
                try 
                {
                    new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
                }
                catch (Exception e) {/* to be frank, I don't care at this point anymore*/}

                // get the string and print it
                String part = new String("");
                try 
                {
                    part = textFileLinePicker.readRandomString(buffer);
                }
                catch (EOFException ef) 
                {
                    readLine.append(ef.getMessage());
                    System.out.println(readLine);
                    System.out.println("I'm out, end of file!");
                    System.exit(1);
                }
                readLine.append(part);
                System.out.print(readLine);
            }
        } 
        catch (IOException ex) 
        {
            System.out.println("Couldn't open the desired file, I'm so, so sorry");
            System.exit(1);
        }
    }

    public static String readRandomString(BufferedReader buff) throws EOFException 
    {
        StringBuilder line = new StringBuilder();

        Random rand = new Random();
        int n = rand.nextInt(5) + 1;

        for (int i = 0; i < n; ++i) 
        {
            try 
            {
                int character = buff.read();
                if (character == -1)
                    break;
                if ((char)character == ' ' || (char)character == '\n') // not gonna get satisfied with whitechars
                    n++;
                line.append((char) character);
            } 
            catch (IOException ex) 
            {
                System.out.println("Couldn't read the chars...lol I don't know");
                System.exit(1);
            }
        }

        // if the end of file was encountered
        if (line.length() < n)
            throw new EOFException(line.toString());

        return line.toString();
    }
}

class simpleGUICauseItsEasierThanPrintingStuffInOneLineLol extends JFrame implements ActionListener 
{
    public JButton button;
    public BufferedReader buffer;

    public simpleGUICauseItsEasierThanPrintingStuffInOneLineLol(File _srcFile) 
    {
        try 
        {
            buffer = new BufferedReader(new FileReader(_srcFile));
        }
        catch (FileNotFoundException e)
        {
            System.out.println("Couldn't open the desired file, I'm so, so sorry");
            System.exit(1);
        }

        setSize(300, 200);
        setLayout(null);
        setTitle("textFileLinePicker");

        button = new JButton("GENERATE");
        button.setBounds(85, 55, 100, 40);
        add(button);
        button.addActionListener(this);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) 
    {
        // get a new part from file and print it
        String part = new String("");
        try 
        {
            part = textFileLinePicker.readRandomString(buffer);
        }
        catch (EOFException ex)
        {
            System.out.print(ex.getMessage());
            System.out.println("\nI'm out, end of file!");
            System.exit(1);
        }
        System.out.print(part);
    }

}