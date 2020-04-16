import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

class DifferentLengthVectors extends Exception 
{
    private int length1;
    private int length2;

    public DifferentLengthVectors(ArrayList<Integer> array1, ArrayList<Integer> array2) 
    {
        length1 = array1.size();
        length2 = array2.size();
    }

    public void command() 
    {
        System.out.println("One's length is " + length1 + ", and the other's is " + length2 + ".");
    }
}

public class AddingVectors 
{
    public static void main(String[] args) 
    {
        Scanner sc = new Scanner(System.in);
        ArrayList<Integer> result = new ArrayList<>();

        while (true) 
        {
            System.out.println("Gimmie 2 VALID vectors, please:");
            ArrayList<Integer> v1 = new ArrayList<>();
            ArrayList<Integer> v2 = new ArrayList<>();

            for (String number : sc.nextLine().split(" "))
            {
                if (number.matches("^-?\\d+"))
                    v1.add(Integer.parseInt(number));
            }
            for (String number : sc.nextLine().split(" ")) 
            {
                if (number.matches("^-?\\d+"))
                    v2.add(Integer.parseInt(number));
            }

            try 
            {
                result = addVectors(v1, v2);
                break;
            } 
            catch (DifferentLengthVectors ex) 
            {
                ex.command();
            }
        }
        saveVectorToFile(result);
    }

    public static void saveVectorToFile(ArrayList<Integer> vector) 
    {
        try 
        {
            BufferedWriter buffer = new BufferedWriter(new FileWriter("saved_vector.txt"));
            for (Integer number : vector) 
            {
                buffer.write(number.toString() + " ");
            }
            buffer.close();
        }
        catch (IOException ex) 
        {
            System.out.println("Couldn't save that vector to file, sorry!");
        }
        System.out.println("Saved that bad boi to file!");
    }

    public static ArrayList<Integer> addVectors(ArrayList<Integer> v1, ArrayList<Integer> v2) throws DifferentLengthVectors 
    {
        if (v1.size() != v2.size())
            throw new DifferentLengthVectors(v1, v2);

        ArrayList<Integer> result = new ArrayList<>();
        for (int i = 0; i < v1.size(); ++i) 
        {
            result.add(v1.get(i) + v2.get(i));
        }

        return result;
    }
}