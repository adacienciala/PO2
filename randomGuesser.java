import java.util.Random;
import java.util.Scanner;
import java.util.InputMismatchException;

public class randomGuesser
{
    public static void main(String[] args)
    {
        Random rand = new Random();
        Scanner sc = new Scanner(System.in);

        int numberRight = rand.nextInt(101);
        int tries = 0;
        boolean end = false;

        System.out.println("Guess the number <0, 100>:");
        while (!end)
        {
            int numberGuessed = 0;
            try
            {
                numberGuessed = sc.nextInt();
                if (numberGuessed < 0 || numberGuessed > 100)
                    throw new InputMismatchException();
            }
            catch (InputMismatchException ex)
            {
                System.out.println("Wrong input, try again!");
                sc.next();
                continue;
            }
            ++tries;
            if (numberGuessed < numberRight)
                System.out.println("Try a bigger number.");
            else if (numberGuessed > numberRight)
                System.out.println("Try a smaller number.");
            else 
            {
                System.out.println("Nice! You guessed on your " + tries + " try!");
                System.out.println("Do you want to play again? Y/N");
                char answer;
                boolean again = false;
                do
                {
                    answer = sc.next().charAt(0);
                    switch (answer)
                    {
                        case 'y':
                        case 'Y':
                            numberRight = rand.nextInt(101);
                            System.out.println("Guess the number <0, 100>:");
                            tries = 0;
                            break;
                        case 'n':
                        case 'N':
                            System.out.println("See you soon!");
                            end = true;
                            break;
                        default:
                            again = true;
                    }
                } while (again);
            }
        }
    }
}