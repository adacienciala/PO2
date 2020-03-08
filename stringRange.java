import java.util.ArrayList;

public class stringRange 
{
    public static void main(String[] args)
    {
        printRange(args);
    }

    public static void validateArray(String[] args)
    {
        if (args.length != 3)
        {
            System.out.println("Wrong number of args!");
            System.exit(1);
        }

        int start = 0, end = 0;
        try
        {
            start = Integer.parseInt(args[1]);
            end = Integer.parseInt(args[2]);
        }
        catch (NumberFormatException excp) 
        {
            System.out.println("Wrong input data!");
            System.exit(1);
        }

        if (start < 0 || end >= args[0].length() || start > end)
        {
            System.out.println("Wrong range numbers!");
            System.exit(1);
        }
    }

    public static void printRange(String[] args)
    {
        validateArray(args);

        int start = Integer.parseInt(args[1]);
        int end = Integer.parseInt(args[2]);
        System.out.println(String.valueOf(args[0].toCharArray(), start, end+1));
    }
}