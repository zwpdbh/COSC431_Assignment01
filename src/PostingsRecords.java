import java.io.Serializable;

/**
 * Created by zw on 19/04/2017.
 */
public class PostingsRecords implements Serializable {
    private long docIDStart;
    private long tfStart;
    private int sizeForDocID;
    private int sizeForTF;

    public PostingsRecords(long docIDStart, long tfStart, int sizeForDocID, int sizeForTF) {
        this.docIDStart = docIDStart;
        this.tfStart = tfStart;
        this.sizeForDocID = sizeForDocID;
        this.sizeForTF = sizeForTF;
    }

    public long getDocIDStart() {
        return docIDStart;
    }

    public void setDocIDStart(long docIDStart) {
        this.docIDStart = docIDStart;
    }

    public long getTfStart() {
        return tfStart;
    }

    public void setTfStart(long tfStart) {
        this.tfStart = tfStart;
    }

    public int getSizeForDocID() {
        return sizeForDocID;
    }

    public void setSizeForDocID(int sizeForDocID) {
        this.sizeForDocID = sizeForDocID;
    }

    public int getSizeForTF() {
        return sizeForTF;
    }

    public void setSizeForTF(int sizeForTF) {
        this.sizeForTF = sizeForTF;
    }

    @Override
    public String toString() {
        return "PostingsRecords{" +
                "docIDStart=" + docIDStart +
                ", tfStart=" + tfStart +
                ", sizeForDocID=" + sizeForDocID +
                ", sizeForTF=" + sizeForTF +
                '}';
    }
}
