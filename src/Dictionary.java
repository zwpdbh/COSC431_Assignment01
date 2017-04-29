import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

/**
 * Created by zw on 16/04/2017.
 */
public class Dictionary {
    /**
     * My data structure after loading the saved data:
     * It is a TreeMap, the key is String, the value is a GroupIndex which will be filled with corresponding HashMap.
     */
    private TreeMap<String, GroupIndex> dictionaryIndex;

    private long numberOfTerms;
    private int numberOfDocuments;

    // Using TF.IDF ranking, showResult specify the number of results will be displayed.
    private int showResult;
    // the array records the corresponding Doc ID to an Integer.
    private ArrayList<String> docIDRecords;
    // specify the absolute path of initialization data.
    private String initializationDataPath;

    /**
     * This constructor need a int to specify the number of results will be displayed.
     * @param showResults specify the number of results will be displayed
     */
    public Dictionary(String initializationDataPath, int showResults) {
        this.numberOfTerms = 0;
        this.initializationDataPath = initializationDataPath;

        if (showResults > 0) {
            this.showResult = showResults;
        } else if (showResults == -1) {
            this.showResult = -1;
        }
        else {
            this.showResult = 20;
        }
        this.docIDRecords = new ArrayList<>();
        this.dictionaryIndex = new TreeMap<>();
    }


    @SuppressWarnings("unchecked")
    public void initializeIndex() throws Exception {

        File indexFile = new File(this.initializationDataPath);
        System.out.println("Loading index file: " + indexFile.getAbsolutePath());

        FileInputStream fis = new FileInputStream(indexFile);
        GZIPInputStream gs = new GZIPInputStream(fis);
        ObjectInputStream ois = new ObjectInputStream(gs);

        long start = System.currentTimeMillis();
        this.numberOfDocuments = (int) ois.readObject();
        this.docIDRecords = (ArrayList<String>) ois.readObject();
        this.numberOfTerms = (long) ois.readObject();
        this.dictionaryIndex = (TreeMap<String, GroupIndex>) ois.readObject();

        System.out.println("\nLoad Index Succeed:");
        System.out.println("Total documents: " + this.numberOfDocuments);
        System.out.println("Total indexed terms: " + this.numberOfTerms);

        long end = System.currentTimeMillis();
        NumberFormat formatter = new DecimalFormat("#0.00000");
        System.out.println("Execution time is " + formatter.format((end - start) / 1000d) + " seconds\n");
    }


    /**
     * search function with a term
     * @param input is string from the input, it will be separated into terms by white blank.
     */
    @SuppressWarnings("unchecked")
    public void searchTerms(String input) {

        long start = System.currentTimeMillis();

        String[] terms = input.toUpperCase().split("[ ]");
        ArrayList<Postings> postingLists = new ArrayList<>();

        if (this.dictionaryIndex.size() == 0) {
            System.out.println("The initialization data is empty, please check whether has loaded the initializationDataPath file correctly");
            return;
        } else {
            for (String eachTerm: terms) {
                try {

                    Entry<String, GroupIndex> subIndex = this.dictionaryIndex.floorEntry(eachTerm);
                    String correspindingMark = subIndex.getKey();
                    GroupIndex correspondingGroupIndex = subIndex.getValue();

                    // if the corresponding HashMap is empty, load it and set the TreeMap with new GroupIndex with HashMap.
                    if (correspondingGroupIndex.subHashMap.size() == 0) {
//                        System.out.println("No corresponding cache, loading: " + correspondingGroupIndex.path);
//                        System.out.println("Load the corresponding HashMap: " + correspondingGroupIndex.path + "/indexed_terms_in_binary");

                        FileInputStream fis = new FileInputStream(correspondingGroupIndex.path +  "/indexed_terms_in_binary");
                        GZIPInputStream gs = new GZIPInputStream(fis);
                        ObjectInputStream ois = new ObjectInputStream(gs);

                        HashMap<String, PostingsRecords> subHashMap = (HashMap<String, PostingsRecords>) ois.readObject();
                        correspondingGroupIndex.setSubHashMap(subHashMap);

                        this.dictionaryIndex.put(correspindingMark, correspondingGroupIndex);

                    }

                    PostingsRecords pr = correspondingGroupIndex.subHashMap.get(eachTerm);

                    if (pr != null) {
                        try {
                            Postings p = readPostings(correspondingGroupIndex.path, pr);
                            if (p != null) {
                                postingLists.add(p);
                            }
                        } catch (IOException io) {
                            System.out.println(io.toString());
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }
        }

        calculateRankAndDisplayResults(postingLists);

        long end = System.currentTimeMillis();
        NumberFormat formatter = new DecimalFormat("#0.00000");
        System.out.print("Execution time is " + formatter.format((end - start) / 1000d) + " seconds\n");
    }



    /**
     * Given PostingsRecords, and the path to stores the "postings_records_for_DocIDs", "postings_records_for_TFs"
     * Return the recovered postings record.
     */
    private static Postings readPostings(String pathToRecords, PostingsRecords pr) throws IOException {
        Postings postings;
        ArrayList<PostingsNode> nodes = new ArrayList<>();

        String recordsForDocIDs = pathToRecords + "/postings_records_for_DocIDs";
        String recordsForTFs = pathToRecords + "/postings_records_for_TFs";

        RandomAccessFile rafDoc = new RandomAccessFile(recordsForDocIDs, "r");
        RandomAccessFile rafTF = new RandomAccessFile(recordsForTFs, "r");

        rafDoc.seek(pr.getDocIDStart());
        rafTF.seek(pr.getTfStart());

        byte[] rafDocIDsCode = new byte[pr.getSizeForDocID()];
        byte[] rafTFsCode = new byte[pr.getSizeForTF()];

        rafDoc.readFully(rafDocIDsCode);
        rafTF.readFully(rafTFsCode);

        List<Integer> docIDs = VBCompression.decode(rafDocIDsCode);
        // from gaps recover back to real docID index value
        docIDs = VBCompression.fromGapsToList(docIDs);

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

    /**
     * display the search results on screen by the TF-IDF ranking
     * @param postingLists are postings list associated with each term.
     */
    private void calculateRankAndDisplayResults(ArrayList<Postings> postingLists) {

        // documentID -- (tf, dft) -> (tf, dft)
        HashMap<Integer, RankList> rankListHashMap = new HashMap<>();

        for (Postings eachPostings: postingLists) {
            int currentDFT = eachPostings.postings.size();
            for (PostingsNode pn : eachPostings.postings) {
                RankList rl = rankListHashMap.get(pn.docID);
                if (rl == null) {
                    rl = new RankList(this.numberOfDocuments);
                }
                rl.addTFandDFT(pn.tf, currentDFT);
                rankListHashMap.put(pn.docID, rl);
            }
        }

        for (Entry<Integer, RankList> entry: rankListHashMap.entrySet()) {
            entry.getValue().computeRSV();
        }

        Map<Integer, RankList> sortedMap = sortByComparator(rankListHashMap);
        printMap(sortedMap);
    }

    /**
     * helper method to sort a HashMap with the computed tf-idf value.
     * @param unsortedMap is the HashMap which it's key is the document ID and
     *                    it's value is the RankList stores the associated (tf, dft) values.
     */
    public static Map<Integer, RankList> sortByComparator(Map<Integer, RankList> unsortedMap) {
        List<Entry<Integer, RankList>> list= new LinkedList<>(unsortedMap.entrySet());

        Collections.sort(list, new Comparator<Entry<Integer, RankList>>() {
            @Override
            public int compare(Entry<Integer, RankList> o1, Entry<Integer, RankList> o2) {
                double v1 = o1.getValue().rsv;
                double v2 = o2.getValue().rsv;
                if (v1 > v2) {
                    return -1;
                } else if (v1 < v2) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        Map<Integer, RankList> sortedMap = new LinkedHashMap<>();
        for (Entry<Integer, RankList> entry: list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    /**
     * print the Hash Map
     * @param map is the Hash Map, key is the docID, value is the RankingList which stores the associated (tf, dft) values.
     */
    private void printMap(Map<Integer, RankList> map) {
        System.out.println("\nRelated documents -> " + map.size());
        int count = 0;
        if (map.size() != 0 && this.showResult != -1) {
            System.out.println("Top " + this.showResult + " results:");
        }
        for (Entry<Integer, RankList> entry: map.entrySet()) {
            System.out.println(this.docIDRecords.get(entry.getKey() - 1) + " : " + entry.getValue().rsv);
            count += 1;
            if (this.showResult != -1 && count >= this.showResult) {
                break;
            }
        }

    }

    /**
     * The RankList is an array list stores TFandDFT which is (tf, dft) pair.
     */
    class RankList implements Comparable<RankList> {
        private ArrayList<TFandDFT> rList;
        private double rsv;
        private int numOfDocs;

        public RankList(int numOfDocs) {
            this.rList = new ArrayList<>();
            this.rsv = 0.0;
            this.numOfDocs = numOfDocs;
        }

        public double computeRSV() {
            double result = 0.0000;
            for (TFandDFT each: this.rList) {
                result += (each.tf * Math.log((float)this.numOfDocs/each.dft));
            }
            this.rsv = result;
            return result;
        }

        public void addTFandDFT(int tf, int dft) {
            this.rList.add(new TFandDFT(tf, dft));
        }

        @Override
        public int compareTo(RankList o) {
            return (int) (this.rsv - o.rsv);
        }
    }

    /**
     * tf is the term frequency, number of occurrences of term t in document d.
     * dft is the term document frequency which is the number of document contains t.
     */
    private static class TFandDFT {
        int tf;
        int dft;

        public TFandDFT(int tf, int dft) {
            this.tf = tf;
            this.dft = dft;
        }
    }

    public static void main(String[] args) {
        Integer resultsToShow = 20;
        String initializationData = "savedInvertedIndex/initializationData";

        if (args.length == 2) {
            try {
                File initializationDataFile = new File(args[0]);
                if (initializationDataFile.exists()) {
                    initializationData = initializationDataFile.getAbsolutePath();
                }
                resultsToShow = Integer.parseInt(args[1]);
            } catch (Exception e) {
                System.out.println(e.toString());

            }
        } else if (args.length == 0) {
            try {
                File initializationDataFile = new File(initializationData);

                if (initializationDataFile.exists()) {
                    initializationData = initializationDataFile.getAbsolutePath();
                }
                resultsToShow = 20;

            } catch (Exception e) {
                System.out.println(e.toString());
            }
        } else {
            System.out.println("Usage:");
            System.out.println("java Dictionary <absolute_path_to_initializationData> <num_of_results_to_show> ");
            return;
        }

        System.out.println();
        System.out.println("Initiate dictionary with parameters:");
        System.out.println("Initialization Data: " + initializationData);
        if (resultsToShow > 0) {
            System.out.println("Number of related search results to show: " + resultsToShow);
        }

        Dictionary d = new Dictionary(initializationData, resultsToShow);


        try {
            d.initializeIndex();
            Scanner inputScan = new Scanner(System.in);
            while (true) {
                System.out.println("\n\nPlease input terms for searching");
                String input = inputScan.nextLine();
                d.searchTerms(input);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }

    }
}