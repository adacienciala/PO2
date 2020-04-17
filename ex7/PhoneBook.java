import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class PhoneBook
{
    private static TreeMap<PhoneNumber, Record> entries;

    public static void main(String[] args)
    {
        entries = new TreeMap<>();

        Person person1 = new Person("Elon", "Musk", "3500 Deer Creek, Palo Alto, CA 94304", new PhoneNumber(1, 6504134000L));
        Person person2 = new Person("Yeshua", "bar Yoseph", "Carpenter Yoseph's house, 16100 Nazareth", new PhoneNumber(9724, 444222777L));
        Company company1 = new Company("Zaklad Pogrzebowy A.S. Bytom", "Leopolda Staffa 1, 41-902 Bytom", new PhoneNumber(48, 884617468L));
        Company company2 = new Company("Hogwart's Castle", "Loch an Eilein, PH22 1UR Aviemore", new PhoneNumber(1, 6054756961L));

        entries.put(person1.getPhoneNumber(), person1);
        entries.put(person2.getPhoneNumber(), person2);
        entries.put(company1.getPhoneNumber(), company1);
        entries.put(company2.getPhoneNumber(), company2);

        printBook();
    }

    public static void printBook()
    {
        Iterator<Map.Entry<PhoneNumber, Record>> it = entries.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry<PhoneNumber, Record> entry = it.next();
            entry.getValue().description();
        }
    }
}

class PhoneNumber implements Comparable 
{
    private Integer countryCode;
    private Long phoneNumber;
    
    public PhoneNumber(Integer countryCode, Long phoneNumber)
    {
        this.countryCode = countryCode;
        this.phoneNumber = phoneNumber;
    }

    public Integer getCountryCode() {
        return countryCode;
    }

    public Long getPhoneNumber() {
        return phoneNumber;
    }

    @Override
    public int compareTo(Object o) 
    {
        if (o instanceof PhoneNumber) 
        {
            int returnValue = this.countryCode.compareTo(((PhoneNumber) o).countryCode);
            if (returnValue == 0) 
                return this.phoneNumber.compareTo(((PhoneNumber) o).phoneNumber);
            return returnValue;
        } else
            throw new ClassCastException();
    }
}

abstract class Record 
{
    public abstract void description();
    abstract public PhoneNumber getPhoneNumber();
}

class Person extends Record 
{
    private String name;
    private String surname;
    private String address;
    private PhoneNumber phoneNumber;

    public Person(String name, String surname, String address, PhoneNumber phoneNumber)
    {
        this.name = name;
        this.surname = surname;
        this.address = address;
        this.phoneNumber = phoneNumber;
    }

    @Override
    public void description() 
    {
        System.out.println("Name:\t\t" + name);
        System.out.println("Surname:\t" + surname);
        System.out.println("Address:\t" + address);
        System.out.println("Phone number:\t+" + phoneNumber.getCountryCode().toString() + ' ' + phoneNumber.getPhoneNumber().toString() + '\n');
    }

    @Override
    public PhoneNumber getPhoneNumber()
    {
        return phoneNumber;
    }
}

class Company extends Record 
{
    private String name;
    private String address;
    private PhoneNumber phoneNumber;

    public Company(String name, String address, PhoneNumber phoneNumber)
    {
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
    }

    @Override
    public void description() 
    {
        System.out.println("Company name:\t" + name);
        System.out.println("Address:\t" + address);
        System.out.println("Phone number:\t+" + phoneNumber.getCountryCode().toString() + ' ' + phoneNumber.getPhoneNumber().toString() + '\n');
    }
    
    @Override
    public PhoneNumber getPhoneNumber()
    {
        return phoneNumber;
    }
}

