import java.io.ObjectOutputStream;

/**
 * <code>hareBox</code> objects is hold the important information about the user.
 * Used in the {@link ServerUnit}'s observable map about the known clients.
 *
 * @author Adrianna Cieńciała
 * @version 1.0
 */
public class RegisteredUser {

    private String username;
    public ObjectOutputStream out;

    public boolean isOnline;

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

    /**
     * Checks if objects are equal, based on the username.
     *
     * @param o RegisteredUser object to compare to.
     * @return True if equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        return username.equals(((RegisteredUser)o).username);
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }
}
