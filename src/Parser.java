import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
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
    private ArrayList<String> marks;

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
                str = fileScan.next().toUpperCase();

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
     * It saves the terms and postings from one part of the HashMap into separately into 3 files. New one
     * 1 is the terms
     * 2 is the postings of docIDs
     * 3 is the postings of tfs.
     *
     * After saving, the indexed_terms_in_binary stores < term, PostingsRecords >
     * PostingsRecords stores the information for RandomAccessFile for docIDs and tfs.
     */
    private void saveHashMapIntoFile(HashMap<String, Postings> hashMap, String path) throws Exception {
        File savingPlace = new File(path);

        try {
            if (!savingPlace.exists()) {
                savingPlace.mkdir();
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }


        HashMap<String, PostingsRecords> termIndex = new HashMap<>();

        String recordsForDocIDs = path + "/postings_records_for_DocIDs";
        String recordsForTFs = path + "/postings_records_for_TFs";

        PositionsForDocIDAndTF p = new PositionsForDocIDAndTF(0, 0, 0, 0);

        for (Entry<String, Postings> entry: hashMap.entrySet()) {
            String term = entry.getKey();
            Postings postings = entry.getValue();

            long docAt = p.docIDsAt;
            long tfAt = p.tfsAt;

            p = savePostingsForDocID(postings, recordsForDocIDs, docAt, recordsForTFs, tfAt);

            termIndex.put(term, new PostingsRecords(docAt, tfAt, p.docIDCodeSize, p.tfCodeSize));
        }


        FileOutputStream fos = new FileOutputStream(path +"/indexed_terms_in_binary");
        GZIPOutputStream gz = new GZIPOutputStream(fos);
        ObjectOutputStream oos = new ObjectOutputStream(gz);
        oos.writeObject(termIndex);

        oos.flush();
        oos.close();
        fos.close();
    }

    /**
     * Slice the big whole HashMap structure into number of groups small HashMap.
     * Meanwhile record each HashMap's smallest (first) term into an ArrayList as marks (one of the property of Parser).
     * @return returns ArrayList of small HashMap
     */
    private ArrayList<HashMap<String, Postings>> getSlicesFromIndex(HashMap<String, Postings> index, int numOfGroups) {

        int groupSize = index.size() / numOfGroups;

        List<Entry<String, Postings>> list = new LinkedList<>(index.entrySet());
        Collections.sort(list, new Comparator<Entry<String, Postings>>() {
            @Override
            public int compare(Entry<String, Postings> o1, Entry<String, Postings> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });

        int counter = 0;
        this.marks = new ArrayList<>();
        ArrayList<HashMap<String, Postings>> groupsOfHashMap = new ArrayList<>();
        HashMap<String, Postings> eachGroupHashMap = new HashMap<>();

        for (Entry<String, Postings> entry: list) {
            if (counter % groupSize == 0) {
                // it is time to collect some index into group to create small hash table
                this.marks.add(entry.getKey());
                if (eachGroupHashMap.size() != 0) {
                    groupsOfHashMap.add(eachGroupHashMap);
                }
                eachGroupHashMap = new HashMap<>();
            }
            eachGroupHashMap.put(entry.getKey(), entry.getValue());
            counter += 1;
        }
        groupsOfHashMap.add(eachGroupHashMap);

        return groupsOfHashMap;
    }

    /**
     * Things to save:
     * 1. postings
     * 2. numberOfDocuments
     * 3. docIDRecords
     * 4. marks
     * 5. save groupIndex / TreeMap
     */
    public void saveIndexInto(String path, int numberofGroups) throws Exception {
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdir();
        }
        if (!directory.isDirectory()) {
            System.err.println("You must specify an absolute directory path to save the index!");
            System.out.println("You are trying to save inverted index into: " + path);
            return;
        }

        System.out.println("Saving Inverted Index");
        long startTime = System.currentTimeMillis();

        ArrayList<HashMap<String, Postings>> groupsOfHashMap = getSlicesFromIndex(this.index, numberofGroups);
        ArrayList<String> coorespondingPath = new ArrayList<>();

        // need to save each hash map
        int counter = 1;
        System.out.println("Will save " + groupsOfHashMap.size() + " sections of HashMap.");
        for (HashMap<String, Postings> subHashMap: groupsOfHashMap) {
            String subDirectory = "/" + counter;
            saveHashMapIntoFile(subHashMap, path + subDirectory);
            coorespondingPath.add(path + subDirectory);
            counter += 1;
        }

        FileOutputStream fos = new FileOutputStream(path + "/initializationData");
        GZIPOutputStream gz = new GZIPOutputStream(fos);
        ObjectOutputStream oos = new ObjectOutputStream(gz);

        TreeMap<String, GroupIndex> dictionaryIndex = new TreeMap<>();

        for (int i = 0; i < this.marks.size(); i++){
            dictionaryIndex.put(this.marks.get(i), new GroupIndex(coorespondingPath.get(i)));
        }

        oos.writeObject(this.numberOfDocuments);
        oos.writeObject(this.docIDRecords);
        oos.writeObject(this.numberOfTerms);
        oos.writeObject(dictionaryIndex);

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
                int numOfGroups = 50;
//                String savingPath = "/Users/zw/Downloads/tmp/newDictionary";
                String savingPath = (System.getProperty("user.dir") + "/savedInvertedIndex");
                p.saveIndexInto(savingPath, numOfGroups);

            } catch (Exception e) {
                System.out.println("Error for saving inverted index: " + e.toString());
            }

        }
    }
}
