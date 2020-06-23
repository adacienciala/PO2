import java.io.ObjectOutputStream;

public class RegisteredUser {

    private String username;
    private ObjectOutputStream out;

    public RegisteredUser(String username, ObjectOutputStream out) {
        this.username = username;
        this.out = out;
    }

    public String getUsername() {
        return username;
    }

    public ObjectOutputStream getOut() {
        return out;
    }

    @Override
    public boolean equals(Object o) {
        return username.equals(this.username);
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }
}
