import java.io.Serializable;
import java.util.ArrayList;

/**
 * <code>PacketObject</code> object structure is shared with the server. Used for server-client communication.
 * This object is used by {@link ClientUnit} and the server's API.
 *
 * @author Adrianna Cieńciała
 * @version 1.0
 */
public class PacketObject implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Indicates the type of the message.
     */
    public enum PACKET_TYPE {
        FILE_UPLOAD,
        FILE_DELETE,
        FILE_SYNCHRONIZE,
        LIST_SYNCHRONIZE
    }

    private PACKET_TYPE type;
    private String recipient;
    private String [] fileList;

    private String fileName;
    private byte[] data;

    private ArrayList<String> userList;

    /**
     * Constructor of this object.
     * Some of the parameters can be null, depending on the type of the packet.
     *
     * @param type Type of the message sent to user.
     * @param recipient User that we want the packet to affect.
     * @param fileList List used for synchronization.
     * @param fileName The name of the file to upload/delete.
     * @param data Data of the included file.
     */
    public PacketObject(PACKET_TYPE type, String recipient, String[] fileList, String fileName, byte[] data) {
        this.type = type;
        this.recipient = recipient;
        this.fileList = fileList;
        this.fileName = fileName;
        this.data = data;
    }

    public void setUserList(ArrayList<String> userList) {
        this.userList = userList;
    }

    public PACKET_TYPE getType() {
        return type;
    }

    public String[] getFileList() { return fileList; }

    public String getRecipient() {
        return recipient;
    }

    public ArrayList<String> getUserList() {
        return userList;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getData() {
        return data;
    }
}
