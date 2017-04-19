/**
 * Created by zw on 17/04/2017.
 */

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A class stores the postings which is an arrayList of (docID, tf)
 */
class Postings implements Serializable {
    ArrayList<PostingsNode> postings;

    public Postings() {
        this.postings = new ArrayList<>();
    }

    public Postings(ArrayList<PostingsNode> postings) {
        this.postings = postings;
    }

    public void addItem(int docID) {
        // if the postings is empty or if the last postings node's docID != the docID, append the docID.
        if (this.postings.isEmpty() || this.postings.get(this.postings.size() -1).docID != docID) {
            this.postings.add(new PostingsNode(docID));
        } else {
            // get the last node in the postings
            PostingsNode pn = this.postings.get(this.postings.size() - 1);
            if (pn.docID == docID) {
                pn.tf += 1;
                this.postings.set(this.postings.size() - 1, pn);
            } else {
                System.out.println("Insert " + docID + " error at insert into postings: ");
                System.out.println(this.toString());
            }
        }
    }


    @Override
    public String toString() {
        String str = "";
        for (PostingsNode pn: this.postings) {
            str += pn.toString();
        }
        return str;
    }
}
