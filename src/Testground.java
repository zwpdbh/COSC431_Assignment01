import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

/**
 * Created by zw on 19/04/2017.
 */
public class Testground {
    public static void main(String args[]) {
        Postings p = new Postings();

        for (int i = 1; i <= 5; i++) {
            p.addItem(i);
        }
        System.out.println("The postings is:");
        System.out.println(p);

        Postings p2 = new Postings();
        for (int i = 1; i <= 7; i++) {
            p2.addItem(i*i);
        }


        String postingsRecords = "postings_records";
        long position;

        int size = p.postings.size();
        int size2 = p2.postings.size();

        try {
            System.out.println("Saving postings records");
            position = savePostings(p, postingsRecords, 0);

            p = null;
            System.out.println("The current content of p is:");
            System.out.println(p);
            System.out.println("Loading postings records:");
            p = readPostings(postingsRecords, 0, size);
            System.out.println(p);



            savePostings(p2, postingsRecords, position);
            p2 = null;
            p2 = readPostings(postingsRecords, position, size2);
            System.out.println(p2);

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

    public static void readFile(String fileName) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
        System.out.println(raf.readInt());

        raf.seek(raf.getFilePointer());
        System.out.println(raf.readInt());

        raf.close();
    }
}
