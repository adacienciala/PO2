import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable, Comparable
{
    private String message;
    private Date scheduledDate;

    public Message(String message, Date scheduledDate)
    {
        this.message = message;
        this.scheduledDate = scheduledDate;
    }

    public String getMessage()
    {
        return message;
    }

    public Date getScheduledDate()
    {
        return scheduledDate;
    }

    @Override
    public int compareTo(Object message2)
    {
        return this.scheduledDate.compareTo(((Message)message2).getScheduledDate());
    }
}
