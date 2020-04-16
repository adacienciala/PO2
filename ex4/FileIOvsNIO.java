import java.io.*;
import java.nio.file.*;
import java.util.Random;

public class FileIOvsNIO
{
    private static String filename;
    private static String data;

    public static void main(String[] args)
    {
        filename = "example";
        randomString(1000);
        long writeTimeIO = writeIO();
        long readTimeIO = readIO();
        long writeTimeNIO = writeNIO();
        long readTimeNIO = readNIO();

        System.out.println();
        System.out.println("Deleting the generated files...");
        deleteFiles();

        System.out.println();
        System.out.println("Write IO:  " + writeTimeIO + "\tRead IO:  " + readTimeIO);
        System.out.println("Write NIO: " + writeTimeNIO + "\tRead NIO: " + readTimeNIO);
    }

    public static String randomString(int number)
    {
        Random rand = new Random();
        StringBuilder randomString = new StringBuilder();
        for (int i=0; i<number; ++i)
        {
            char a = (char)(rand.nextInt('z'-'a' + 1) + 'a');
            randomString.append(a);
        }
        data = randomString.toString();
        return data;
    }

    public static long writeIO()
    {
        System.out.println("~~~~~~~ WRITING TO FILE [IO] ~~~~~~~");
        long timeStart = System.currentTimeMillis();
        try
        {
            BufferedWriter buffer = new BufferedWriter(new FileWriter(filename + "IO.txt"));
            buffer.write(data);
            buffer.close();
        }
        catch (IOException ex)
        {
            System.out.println("Something's wrong with writing to file [IO]");
            System.exit(1);
        }
        long timeEnd = System.currentTimeMillis();
        return timeEnd-timeStart;
    }

    public static long readIO()
    {
        System.out.println("~~~~~~ READING FROM FILE [IO] ~~~~~~");
        StringBuilder dataRead = new StringBuilder();
        long timeStart = System.currentTimeMillis();
        try
        {
            BufferedReader buffer = new BufferedReader(new FileReader(filename + "IO.txt"));
            String s;
            while ((s=buffer.readLine())!=null)
            {
                dataRead.append(s);
            }
            buffer.close();
        }
        catch (IOException ex)
        {
            System.out.println("Something's wrong with reading file [IO]");
            System.exit(1);
        }
        long timeEnd = System.currentTimeMillis();
        System.out.println("DATA READ:\n" + dataRead + '\n');
        return timeEnd-timeStart;
    }

    public static long writeNIO()
    {
        System.out.println("~~~~~~ WRITING TO FILE [NIO] ~~~~~~~");
        Path path = Paths.get(filename + "NIO.txt");
        long timeStart = System.currentTimeMillis();
        try
        {
            Files.write(path, data.getBytes());
        }
        catch (IOException ex)
        {
            System.out.println("Something's wrong with writing to file [NIO]");
            System.exit(1);
        }
        long timeEnd = System.currentTimeMillis();
        return timeEnd-timeStart;
    }

    public static long readNIO()
    {
        System.out.println("~~~~~ READING FROM FILE [NIO] ~~~~~~");
        byte[] dataRead = {};
        long timeStart = System.currentTimeMillis();
        try
        {
            Path path = Paths.get(filename + "NIO.txt");
            dataRead = Files.readAllBytes(path);
        }
        catch (IOException ex)
        {
            System.out.println("Something's wrong with reading file [NIO]");
            System.exit(1);
        }
        long timeEnd = System.currentTimeMillis();
        System.out.println("DATA READ:\n" + new String(dataRead) + '\n');
        return timeEnd-timeStart;
    }

    public static void deleteFiles()
    {
        File f = new File(filename + "IO.txt");
        if (!f.delete()) System.out.println("Couldn't delete IO file");
        f = new File(filename + "NIO.txt");
        if (!f.delete()) System.out.println("Couldn't delete NIO file");
    }
}