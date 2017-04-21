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
    private HashMap<String, PostingsRecords> index;
    // the index data is a Hash table.
    private long numberOfTerms;
    private int numberOfDocuments;

    // Using TF.IDF ranking, showResult specify the number of results will be displayed.
    private int showResult;
    private ArrayList<String> docIDRecords;

    private String indexedTermRecords;
    private String postingsForDocIDs;
    private String postingsForTFs;

    /**
     * This constructor need a int to specify the number of results will be displayed.
     * @param showResults specify the number of results will be displayed
     */
    public Dictionary(String indexedTermRecords, String postingsForDocIDs, String postingsForTFs, int showResults) {
        this.index = new HashMap<>();
        numberOfTerms = 0;
        if (showResults > 0) {
            this.showResult = showResults;
        } else if (showResults == -1) {
            this.showResult = -1;
        }
        else {
            this.showResult = 20;
        }
        this.docIDRecords = new ArrayList<>();
        this.indexedTermRecords = indexedTermRecords;
        this.postingsForDocIDs = postingsForDocIDs;
        this.postingsForTFs = postingsForTFs;
    }


    /**
     * load indexed_terms and read postings_records_in_binary_file for searched every term
     */
    @SuppressWarnings("unchecked")
    public void loadIndexData() throws Exception {
        File indexFile = new File(this.indexedTermRecords);
        System.out.println("Loading index file: " + indexFile.getAbsolutePath());

        FileInputStream fis = new FileInputStream(indexFile);
        GZIPInputStream gs = new GZIPInputStream(fis);
        ObjectInputStream ois = new ObjectInputStream(gs);

        long start = System.currentTimeMillis();
        this.index = (HashMap<String, PostingsRecords>) ois.readObject();
        this.numberOfDocuments = (int) ois.readObject();
        this.docIDRecords = (ArrayList<String>) ois.readObject();
        this.numberOfTerms = this.index.size();

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
    public void searchWithTerms(String input) {

        long start = System.currentTimeMillis();

        String[] terms = input.toUpperCase().split("[ ]");
        ArrayList<Postings> postingLists = new ArrayList<>();

        if (this.index.size() == 0) {
            System.out.println("The loaded index is empty, please check whether has loaded the indexed_terms file correctly");
            return;
        } else {
            for (String eachTerm: terms) {
                PostingsRecords pr = this.index.get(eachTerm.trim());
                if (pr != null) {
                    try {
                        Postings p = readPostings(this.postingsForDocIDs, this.postingsForTFs, pr);
                        if (p != null) {
                            postingLists.add(p);
                        }
                    } catch (IOException io) {
                        System.out.println(io.toString());
                    }
                }
            }
        }

        calculateRankAndDisplayResults(postingLists);

        long end = System.currentTimeMillis();
        NumberFormat formatter = new DecimalFormat("#0.00000");
        System.out.print("Execution time is " + formatter.format((end - start) / 1000d) + " seconds\n");
    }

    /**
     * Need to test if the frequently open and close a file will slow down the speed.
     */
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
    private static Map<Integer, RankList> sortByComparator(Map<Integer, RankList> unsortedMap) {
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
        String indexedTermRecords = "indexed_terms_in_binary";
        String postingsForDocIDs = "postings_records_for_DocIDs";
        String postingsForTFs = "postings_records_for_TFs";

        if (args.length == 4) {
            try {
                File indexedTermRecordsFile = new File(args[0]);
                if (indexedTermRecordsFile.exists()) {
                    indexedTermRecords = indexedTermRecordsFile.getAbsolutePath();
                }

                File docIDRecords = new File(args[1]);
                if (docIDRecords.exists()) {
                    postingsForDocIDs = docIDRecords.getAbsolutePath();
                }

                File tfRecords = new File(args[2]);
                if (tfRecords.exists()) {
                    postingsForTFs = tfRecords.getAbsolutePath();
                }

                resultsToShow = Integer.parseInt(args[3]);

            } catch (Exception e) {
                System.out.println(e.toString());

            }
        } else if (args.length == 0) {
            try {
                File indexedTermRecordsFile = new File(indexedTermRecords);
                if (indexedTermRecordsFile.exists()) {
                    indexedTermRecords = indexedTermRecordsFile.getAbsolutePath();
                }

                File docIDRecords = new File(postingsForDocIDs);
                if (docIDRecords.exists()) {
                    postingsForDocIDs = docIDRecords.getAbsolutePath();
                }

                File tfRecords = new File(postingsForTFs);
                if (tfRecords.exists()) {
                    postingsForTFs = tfRecords.getAbsolutePath();
                }
                resultsToShow = 20;

            } catch (Exception e) {
                System.out.println(e.toString());
            }
        } else {
            System.out.println("Usage:");
            System.out.println("java Dictionary <terms_records> <docIDs_records> <tfs_records> <num_of_results_to_show> ");
            return;
        }

        System.out.println();
        System.out.println("Initiate dictionary with parameters:");
        System.out.println("Index term: " + indexedTermRecords);
        System.out.println("DocIDs records: " + postingsForDocIDs);
        System.out.println("TFs records: " + postingsForTFs);
        if (resultsToShow > 0) {
            System.out.println("Number of related search results to show: " + resultsToShow);
        }

        Dictionary d = new Dictionary(indexedTermRecords, postingsForDocIDs, postingsForTFs, resultsToShow);

        try {
            d.loadIndexData();
            Scanner inputScan = new Scanner(System.in);
            while (true) {
                System.out.println("\n\nPlease input terms for searching");
                String input = inputScan.nextLine();
                d.searchWithTerms(input);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }

    }
}