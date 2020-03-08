import java.util.ArrayList;

public class quadraticEquation
{
    public static void main(String[] args) 
    {
        if (args.length != 3) 
        {
            System.out.println("Wrong number of args!\n");
            return;
        }

        ArrayList<Integer> numbers = new ArrayList<Integer>();
        for (String arg : args)
        {
            numbers.add(convertStringToInteger(arg));
        }

        printEquation(numbers);
        solveEquation(numbers);
    }

    public static Integer convertStringToInteger(String arg)
    {
        Integer num = 0;
        try 
        {
            num = Integer.parseInt(arg);
        } 
        catch (NumberFormatException excp) 
        {
            System.out.println(arg + " is not a number\n");
            System.exit(1);
        }
        return num;
    }

    public static void solveEquation(ArrayList<Integer> numbers)
    {
        int a = numbers.get(0);
        int b = numbers.get(1);
        int c = numbers.get(2);

        double delta = Math.abs(b*b-4*a*c);
        double x1 = (-1*b-Math.sqrt(delta))/(2*a);
        double x2 = (-1*b+Math.sqrt(delta))/(2*a);

        System.out.println("Results:");
        System.out.printf("\tx1 = %.3f\n", x1);
        System.out.printf("\tx2 = %.3f\n", x2);
    }

    public static void printEquation(ArrayList<Integer> numbers)
    {
        System.out.print("Equation:\n\t");
        for (int i = 0, ex = numbers.size()-1; i < numbers.size(); ++i, --ex) 
        {
            int number = numbers.get(i);
            if (number > 0 && i != 0)
            {
                System.out.print("+");
            }
            if (number != 0)
            {
                if (i == 0)
                    System.out.print(number + "x^" + ex);
                else if (i == numbers.size()-1)
                    System.out.print(number);
                else
                    System.out.print(number + "x^" + ex);
            }
        }
        System.out.print("\n");
    }
}