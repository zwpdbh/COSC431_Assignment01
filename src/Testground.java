import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zw on 19/04/2017.
 */
public class Testground {
    public static void main(String args[]) {
//        Postings p = new Postings();
//
//        for (int i = 1; i <= 5; i++) {
//            p.addItem(i);
//        }
//        System.out.println("The postings is:");
//        System.out.println(p);
//
//        Postings p2 = new Postings();
//        for (int i = 1; i <= 7; i++) {
//            p2.addItem(i*i);
//        }
//
//
//        String postingsRecords = "postings_records";
//        long position;
//
//        int size = p.postings.size();
//        int size2 = p2.postings.size();
//
//        try {
//            System.out.println("Saving postings records");
//            position = savePostings(p, postingsRecords, 0);
//
//            p = null;
//            System.out.println("The current content of p is:");
//            System.out.println(p);
//            System.out.println("Loading postings records:");
//            p = readPostings(postingsRecords, 0, size);
//            System.out.println(p);
//
//
//
//            savePostings(p2, postingsRecords, position);
//            p2 = null;
//            p2 = readPostings(postingsRecords, position, size2);
//            System.out.println(p2);
//
//        } catch (IOException io) {
//            System.out.println(io.toString());
//        }

        Postings p = new Postings();
        for (int i = 1; i <=5; i++) {
            p.addItem(i);
        }
        System.out.println("postings is: ");
        System.out.println(p);

        String docIDRecords = "docIDRecords";
        String tfRecords = "tfRecords";

        PositionsForDocIDAndTF positions = new PositionsForDocIDAndTF(0, 0, 0, 0);
        try {
            System.out.println("saving postings records");
            positions = savePostingsForDocID(p, docIDRecords, 0, tfRecords, 0);
        } catch (IOException io) {
            System.out.println(io.toString());
        }


        try {
            System.out.println("Reading records");
            p = null;
            p = readPostings(docIDRecords, tfRecords, new PostingsRecords(0, 0, positions.docIDCodeSize, positions.tfCodeSize));
            System.out.println("postings is:");
            System.out.println(p);
        } catch (IOException io) {
            System.out.println(io.toString());
        }

    }

    public static long savePostings(Postings p, String recordFile, long start) throws IOException {

        RandomAccessFile raf = new RandomAccessFile(recordFile, "rw");
        raf.seek(start);

        for (PostingsNode pn: p.postings) {
            Integer docID = pn.getDocID();
            Integer tf = pn.getTf();

            raf.writeInt(docID);
            raf.seek(raf.getFilePointer());

            raf.writeInt(tf);
            raf.seek(raf.getFilePointer());
        }

        long end = raf.getFilePointer();
        raf.close();

        return end;
    }

    public static Postings readPostings(String recordsFile, long start, int size) throws IOException {
        Postings p;
        ArrayList<PostingsNode> nodes = new ArrayList<>();

        RandomAccessFile raf = new RandomAccessFile(recordsFile, "r");

        raf.seek(start);

        for (int i = 1; i <= size; i++) {
            int docID = raf.readInt();
            raf.seek(raf.getFilePointer());

            int tf = raf.readInt();
            raf.seek(raf.getFilePointer());

            nodes.add(new PostingsNode(docID, tf));
        }
        raf.close();
        p = new Postings(nodes);

        return p;
    }

    public static PositionsForDocIDAndTF savePostingsForDocID(Postings p, String recordsForDocID, long docIDAt, String recordsForTF, long tfAt) throws IOException {
        RandomAccessFile rafDocID = new RandomAccessFile(recordsForDocID, "rw");
        RandomAccessFile rafTF = new RandomAccessFile(recordsForTF, "rw");

        rafDocID.seek(docIDAt);
        rafTF.seek(tfAt);

        ArrayList<Integer> docIDs = new ArrayList<>();
        ArrayList<Integer> tfs = new ArrayList<>();

        for (PostingsNode pn: p.postings) {
            docIDs.add(pn.docID);
            tfs.add(pn.tf);
        }

        byte[] docIDsCode = VBCompression.encode(docIDs);
        byte[] tfsCode = VBCompression.encode(tfs);

        rafDocID.write(docIDsCode);
        rafTF.write(tfsCode);

        long docIDEnd = rafDocID.getFilePointer();
        long tfEnd = rafTF.getFilePointer();

        rafDocID.close();
        rafTF.close();

        return new PositionsForDocIDAndTF(docIDEnd, tfEnd, docIDsCode.length, tfsCode.length);
    }

    private static Postings readPostings(String recordsForDocIDs, String recordsForTFs, PostingsRecords pr) throws IOException {
        Postings postings;
        ArrayList<PostingsNode> nodes = new ArrayList<>();

        RandomAccessFile rafDoc = new RandomAccessFile(recordsForDocIDs, "r");
        RandomAccessFile rafTF = new RandomAccessFile(recordsForTFs, "r");

        rafDoc.seek(pr.getDocIDStart());
        rafTF.seek(pr.getTfStart());

        byte[] rafDocIDsCode = new byte[pr.getSizeForDocID()];
        byte[] rafTFsCode = new byte[pr.getSizeForTF()];

        rafDoc.readFully(rafDocIDsCode);
        rafTF.readFully(rafTFsCode);

        List<Integer> docIDs = VBCompression.decode(rafDocIDsCode);
        List<Integer> tfs = VBCompression.decode(rafTFsCode);

        if (docIDs.size() == tfs.size()) {
            for (int i = 0; i < docIDs.size(); i++) {
                nodes.add(new PostingsNode(docIDs.get(i), tfs.get(i)));
            }
            postings = new Postings(nodes);
            return postings;
        } else {
            System.out.println("Error, when randomAccess to read records and decode.");
            System.out.println("The number of docIDs don't match the tfs, they should be same");
            return null;
        }
    }

    private static class PositionsForDocIDAndTF {
        long docIDsAt;
        long tfsAt;

        int docIDCodeSize;
        int tfCodeSize;

        public PositionsForDocIDAndTF(long docIDAt, long tfAt, int docIDCodeSize, int tfCodeSize) {
            this.docIDsAt = docIDAt;
            this.tfsAt = tfAt;
            this.docIDCodeSize = docIDCodeSize;
            this.tfCodeSize = tfCodeSize;
        }
    }


}
