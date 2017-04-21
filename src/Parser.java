import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.zip.GZIPOutputStream;
import java.util.Map.Entry;

/**
 * Created by zw on 19/04/2017.
 */
public class Parser {
    private HashMap<String, Postings> index;
    private long numberOfTerms;
    private int numberOfDocuments;
    private ArrayList<String> docIDRecords;

    public Parser() {
        this.index = new HashMap<>();
        this.numberOfTerms = 0;
        this.docIDRecords = new ArrayList<>();
    }

    /**
     * It continues to read a string, then after string processing.
     * The string will be added into Index as a term.
     */
    public void parseXML(String url) {
        Scanner fileScan;
        String str = "";
        String docID = "";

        try {
            PrintWriter output = new PrintWriter("tokens.txt");
            fileScan = new Scanner(new File(url));

            // record the starting time
            long start = System.currentTimeMillis();
            System.out.print("Parsing XML...");
            System.out.println("Meanwhile saving the processing tokens into: " + "tokens.txt");
            while (fileScan.hasNext()) {
                // 1. to lowercase
                str = fileScan.next().toLowerCase();

                if (Util.isDocumentNumber(str)) {
                    docID = str;
                    docIDRecords.add(docID);
                    numberOfDocuments += 1;
                    output.println("\n" + docID);
                    continue;
                }

                for (String each : Util.getToken(str)) {
                    output.println(each);
                    Postings p;
                    // get the postings associated with the term
                    p = this.index.get(each);

                    try {
                        // if the postings is null, means there is no record for such term
                        if (p == null) {
                            // create a postings, the number of terms += 1
                            p = new Postings();
                            this.numberOfTerms += 1;
                        }
                        // if the postings is not null, means there has been the record for such term, add the docID into postings
                        p.addItem(numberOfDocuments);
                        // after changing the postings, update the HashMap
                        this.index.put(each, p);
                    } catch (NullPointerException e) {
                        System.out.println(e);
                    }

                }
            }
            output.close();
            System.out.println("Parse XML file: " + url + "\nSucceed!");
            System.out.println("Total docs: " + numberOfDocuments);
            System.out.println("Total terms: " + this.numberOfTerms);
            // record the end time.
            long end = System.currentTimeMillis();
            NumberFormat formatter = new DecimalFormat("#0.00000");
            System.out.println("Execution time is " + formatter.format((end - start) / 1000d) + " seconds\n");
        } catch (FileNotFoundException e) {
            System.err.println(String.format("Error occurs when trying to open xml file: %s", e));
        }
    }


    /**
     * Save a postings into two different file, one for docID, another for tf.
     * @param p postings.
     * @param recordsForDocID is the fileName for saving docIDs.
     * @param docIDAt is the file pointer for randomAccess recordsForDocID.
     * @param recordsForTF is the fileName for saving tfs.
     * @param tfAt is the file pointer position for randomAccess recordsForTF.
     * @return PositionsForDocIDAndTF
     */
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

        // saving gaps instead of docIDs
        docIDs = VBCompression.fromListToGaps(docIDs);

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

    /**
     * Stores the information for RandomAccessFile, the position to start and the size of bytes need to read.
     */
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

    /**
     * It saves the terms and postings separately into 3 files.
     * 1 is the terms
     * 2 is the postings of docIDs
     * 3 is the postings of tfs.
     *
     * After saving, the indexed_terms_in_binary stores < term, PostingsRecords >
     * PostingsRecords stores the information for RandomAccessFile for docIDs and tfs.
     */
    public void saveInvertedIndex() throws Exception {
        System.out.println("Saving Inverted Index...");

        long startTime = System.currentTimeMillis();

        HashMap<String, PostingsRecords> termIndex = new HashMap<>();
        String recordsForDocIDs = "postings_records_for_DocIDs";
        String recordsForTFs = "postings_records_for_TFs";

        PositionsForDocIDAndTF p = new PositionsForDocIDAndTF(0, 0, 0, 0);

        for (Entry<String, Postings> entry: this.index.entrySet()) {
            String term = entry.getKey();
            Postings postings = entry.getValue();

            long docAt = p.docIDsAt;
            long tfAt = p.tfsAt;

            p = savePostingsForDocID(postings, recordsForDocIDs, docAt, recordsForTFs, tfAt);

            termIndex.put(term, new PostingsRecords(docAt, tfAt, p.docIDCodeSize, p.tfCodeSize));
        }


        FileOutputStream fos = new FileOutputStream("indexed_terms_in_binary");
        GZIPOutputStream gz = new GZIPOutputStream(fos);
        ObjectOutputStream oos = new ObjectOutputStream(gz);
        oos.writeObject(termIndex);
        oos.writeObject(this.numberOfDocuments);
        oos.writeObject(this.docIDRecords);

        oos.flush();
        oos.close();
        fos.close();

        long endTime = System.currentTimeMillis();
        NumberFormat formatter = new DecimalFormat("#0.00000");
        System.out.println("Execution time is " + formatter.format((endTime - startTime) / 1000d) + " seconds\n");
    }


    public static void main(String[] args) {
        System.out.println("The parser will parse the XML file and save the inverted index into three parts:");
        Parser p = new Parser();
        if (args.length != 1) {
            System.out.println("Please specify the XML file to parse");
            System.out.println("Such as:\njava Parser /absolute_path/your_data.xml");
        } else {
            p.parseXML(args[0]);
            try {
                p.saveInvertedIndex();
            } catch (Exception e) {
                System.out.println("Error for saving inverted index: " + e.toString());
            }

        }
    }
}
