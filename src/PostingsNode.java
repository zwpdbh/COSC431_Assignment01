/**
 * Created by zw on 17/04/2017.
 */

import java.io.Serializable;

/**
 * (docID, tf)
 */
class PostingsNode implements Serializable {
    int docID;
    int tf;

    public PostingsNode(int docID) {
        this.docID = docID;
        this.tf = 1;
    }

    public PostingsNode(int docID, int tf) {
        this.docID = docID;
        this.tf = tf;
    }

    public int getDocID() {
        return docID;
    }

    public int getTf() {
        return tf;
    }

    @Override
    public String toString() {
        return "->(" + this.docID + ", " + this.tf + ")";
    }
}