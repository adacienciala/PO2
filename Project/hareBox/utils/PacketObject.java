import java.io.Serializable;

public class PacketObject implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum PACKET_TYPE {
        FILE_UPLOAD,
        //FILE_MODIFY,
        FILE_DELETE
    }

    private PACKET_TYPE type;
    private String recipient;

    private String fileName;
    private byte[] data;

    public PacketObject(PACKET_TYPE type, String recipient, String fileName, byte[] data) {
        this.type = type;
        this.recipient = recipient;
        this.fileName = fileName;
        this.data = data;
    }

    public PACKET_TYPE getType() {
        return type;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getData() {
        return data;
    }
}
