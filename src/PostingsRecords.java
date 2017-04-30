import java.io.Serializable;

/**
 * Created by zw on 19/04/2017.
 */

/**
 * A PostingsRecords, simply stores the positions of file pointers which used to
 * read the docID and term frequency in a binary file.
 * Given a search term, HashMap will return a PostingsRecords which will be used
 * to get the corresponding postings information.
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


    public long getTfStart() {
        return tfStart;
    }


    public int getSizeForDocID() {
        return sizeForDocID;
    }


    public int getSizeForTF() {
        return sizeForTF;
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
