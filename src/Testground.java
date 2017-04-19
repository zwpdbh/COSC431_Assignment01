import java.io.File;
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

        System.out.println(p);


        String fileName = "randomAccessFile";
        try {
            savePostings(p, fileName);

            readFile(fileName);
        } catch (IOException io) {
            System.out.println(io.toString());
        }
    }

    public static void savePostings(Postings p, String fileName) throws IOException {

        RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
//        for (PostingsNode pn: p.postings) {
//            Integer docID = pn.getDocID();
//            raf.writeInt(docID);
//        }
        raf.writeInt(11);
        raf.seek(raf.getFilePointer());

        raf.writeInt(22);

        raf.close();

    }

    public static void readFile(String fileName) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
        System.out.println(raf.readInt());

        raf.seek(raf.getFilePointer());
        System.out.println(raf.readInt());

        raf.close();
    }
}
