import java.io.*;
import java.lang.instrument.Instrumentation;
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
     * need to check whether the frequently open and close will slow down the speed.
     */
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


    /**
     * It saves the terms and postings separately.
     * First, create a new hash table, with key = term, value = (start, length)
     * Second, literate through the this.index hash table, for each Entry: term - postings
     * serialize postings, which get a start and length in binary file.
     * use those values to insert Entry into hash table.
     * Third, serialize the new Hash table.
     */
    public void saveInvertedIndex() throws Exception {
        System.out.println("Saving Inverted Index...");

        long startTime = System.currentTimeMillis();

        HashMap<String, PostingsRecords> termIndex = new HashMap<>();


        long position = 0;
        for (Entry<String, Postings> entry: this.index.entrySet()) {
            String term = entry.getKey();
            Postings p = entry.getValue();

            termIndex.put(term, new PostingsRecords(position, p.postings.size()));
            position = savePostings(p, "postings_records_in_binary", position);
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


//    /**
//     * print the inverted index into a file.
//     * @param url specify the txt file used for displaying inverted index.
//     */
//    private void printOutIndexResultsIntoFile(String url) {
//        try {
//            PrintWriter output = new PrintWriter(url);
//            System.out.println("write index structure into .txt file: " + url);
//            long start = System.currentTimeMillis();
//            for (Map.Entry<String, Postings> entry: this.index.entrySet()) {
//                output.println(entry.getKey() + entry.getValue().toString());
//            }
//            output.close();
//            long end = System.currentTimeMillis();
//            NumberFormat formatter = new DecimalFormat("#0.00000");
//            System.out.print("Execution time is " + formatter.format((end - start) / 1000d) + " seconds\n");
//        } catch (FileNotFoundException e) {
//            System.out.println(e);
//        }
//    }


    public static void main(String[] args) {
        System.out.println("The parser will parse the XML file and save the inverted index into two parts:");
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
