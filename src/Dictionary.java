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
    private String postingsRecords;

    /**
     * This constructor need a int to specify the number of results will be displayed.
     * @param showResults specify the number of results will be displayed
     */
    public Dictionary(String indexedTermRecords, String postingsRecords, int showResults) {
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
        this.postingsRecords = postingsRecords;
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

        String[] terms = input.split("[ ]");
        ArrayList<Postings> postingLists = new ArrayList<>();

        if (this.index.size() == 0) {
            System.out.println("The loaded index is empty, please check whether has loaded the indexed_terms file correctly");
            return;
        } else {
            for (String eachTerm: terms) {
                PostingsRecords pr = this.index.get(eachTerm.trim());
                if (pr != null) {
                    try {
                        Postings p = readPostings(this.postingsRecords, pr);
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
    private static Postings readPostings(String recordsFile, PostingsRecords pr) throws IOException {
        Postings p;
        ArrayList<PostingsNode> nodes = new ArrayList<>();

        RandomAccessFile raf = new RandomAccessFile(recordsFile, "r");

        raf.seek(pr.start);

        for (int i = 1; i <= pr.size; i++) {
            int docID = raf.readInt();
            int tf = raf.readInt();
            nodes.add(new PostingsNode(docID, tf));
        }
        raf.close();
        p = new Postings(nodes);

        return p;
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
                double v1 = o1.getValue().computeRSV();
                double v2 = o2.getValue().computeRSV();
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
        ArrayList<TFandDFT> rList;
        double rsv = 0.0;
        int numOfDocs;

        public RankList(int numOfDocs) {
            this.rList = new ArrayList<>();
            this.rsv = 0.0;
            this.numOfDocs = numOfDocs;
        }

        public double computeRSV() {
            double result = 0.0;
            for (TFandDFT each: this.rList) {
                result += (each.tf * Math.log(this.numOfDocs/each.dft));
            }
            this.rsv = result;
            return result;
        }

        public void addTFandDFT(int tf, int dft) {
            this.rList.add(new TFandDFT(tf, dft));
        }

        @Override
        public int compareTo(RankList o) {
            return (int) (this.computeRSV() - o.computeRSV());
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
        String postingsRecords = "postings_records_in_binary";

        if (args.length == 3) {
            try {
                File indexedTermRecordsFile = new File(args[0]);
                if (indexedTermRecordsFile.exists()) {
                    indexedTermRecords = indexedTermRecordsFile.getAbsolutePath();
                }

                File postingsRecordsFile = new File(args[1]);
                if (postingsRecordsFile.exists()) {
                    postingsRecords = postingsRecordsFile.getAbsolutePath();
                }

                resultsToShow = Integer.parseInt(args[2]);

            } catch (Exception e) {
                System.out.println(e.toString());
            }
        } else {
            System.out.println("Usage:");
            System.out.println("java Dictionary <terms_records> <postings_records> <num_of_results_to_show> ");
            return;
        }

        System.out.println();
        System.out.println("Initiate dictionary with parameters:");
        System.out.println("Index term: " + indexedTermRecords);
        System.out.println("Postings records: " + postingsRecords);
        if (resultsToShow <= 0) {
            System.out.println("Number of related search results to show: " + resultsToShow);
        }

        Dictionary d = new Dictionary(indexedTermRecords, postingsRecords, resultsToShow);

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