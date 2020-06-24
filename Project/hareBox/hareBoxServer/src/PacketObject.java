import java.io.Serializable;
import java.util.ArrayList;

public class PacketObject implements Serializable {

    private static final long serialVersionUID = 1L;

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
