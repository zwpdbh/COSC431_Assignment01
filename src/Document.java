import java.io.Serializable;

/**
 * Created by zw on 21/04/2017.
 */
public class Document implements Serializable {
    int documentLength;
    String docID;

    public Document(String docID) {
        this.docID = docID;
        this.documentLength = 0;
    }

    public void countDocumentLength() {
        this.documentLength += 1;
    }

    public int getDocumentLength() {
        return documentLength;
    }

    @Override
    public String toString() {
        return this.docID;
    }
}