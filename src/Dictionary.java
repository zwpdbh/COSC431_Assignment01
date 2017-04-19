import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    /**
     * This constructor need a int to specify the number of results will be displayed.
     * @param showResults specify the number of results will be displayed
     */
    public Dictionary(int showResults) {
        this.index = new HashMap<>();
        numberOfTerms = 0;
        this.showResult = showResults;
        this.docIDRecords = new ArrayList<>();
    }


    /**
     * load indexed_terms and read postings_records_in_binary_file for searched every term
     */
    @SuppressWarnings("unchecked")
    public void loadIndexData() throws Exception {
        File indexFile = new File("indexed_terms");
        System.out.println("Loading index file: " + indexFile.getAbsolutePath());

        FileInputStream fis = new FileInputStream(indexFile);
        GZIPInputStream gs = new GZIPInputStream(fis);
        ObjectInputStream ois = new ObjectInputStream(gs);

        long start = System.currentTimeMillis();
        this.index = (HashMap<String, PostingsRecords>) ois.readObject();
        this.numberOfDocuments = (int) ois.readObject();
        this.docIDRecords = (ArrayList<String>) ois.readObject();


        long end = System.currentTimeMillis();
        NumberFormat formatter = new DecimalFormat("#0.00000");
        System.out.println("Execution time is " + formatter.format((end - start) / 1000d) + " seconds\n");


    }

    /**
     * search function with a term
     * @param input is string from the input, it will be separated into terms by white blank.
     */
    public void searchWithTerms(String input) {
//        long start = System.currentTimeMillis();
//        String[] terms = input.split("[ ]");
//        ArrayList<Postings> postingLists = new ArrayList<>();
//
//        // compute the list.
//        for (String each: terms) {
//            Postings p = this.index.get(each);
//
//            if (p != null) {
//                postingLists.add(p);
//            }
//        }
//
//        displayRankedResult(postingLists);
//
//        long end = System.currentTimeMillis();
//        NumberFormat formatter = new DecimalFormat("#0.00000");
//        System.out.print("Execution time is " + formatter.format((end - start) / 1000d) + " seconds\n");
        long start = System.currentTimeMillis();

        String[] terms = input.split("[ ]");
        ArrayList<Postings> postingLists = new ArrayList<>();

        if (this.index.size() == 0) {
            System.out.println("The loaded index is empty, please check whether has loaded the indexed_terms file correctly");
            return;
        } else {
            for (String eachTerm: terms) {
                PostingsRecords postingsRecords = this.index.get(eachTerm);
                if (postingsRecords != null) {
                    System.out.println(postingsRecords);
                    Postings p = getPostingsFromPostingsRecords(postingsRecords);
                    System.out.println(p);
                    if (p != null) {
                        postingLists.add(p);
                    }
                }
            }
        }

        calculateRankAndDisplayResults(postingLists);

        long end = System.currentTimeMillis();
        NumberFormat formatter = new DecimalFormat("#0.00000");
        System.out.print("Execution time is " + formatter.format((end - start) / 1000d) + " seconds\n");
    }

    private Postings getPostingsFromPostingsRecords(PostingsRecords postingsRecords) {

//        Postings p = new Postings();
//        Path filepath;
//        int count;
//
//        // first obtain a path to the binary file.
//        try {
//            filepath = Paths.get("postings_records_in_binary_file");
//        } catch (InvalidPathException e) {
//            System.out.println("Path Error " + e);
//            return p;
//        }
//
//        // next, obtain a channel to that file within a try-with-resources block.
//        try (SeekableByteChannel fChan = Files.newByteChannel(filepath)){
//
//            // allocate a buffer
//            fChan.position(postingsRecords.start);
//            ByteBuffer buffer = ByteBuffer.allocate(postingsRecords.length);
//            byte[] content = new  byte[postingsRecords.length];
//
//
//            count = fChan.read(buffer);
//
//            // stop when end of file is reached
//            if (count != 1) {
//                buffer.rewind();
//                for (int i = 0; i < count; i++) {
//                    content[i] = buffer.get();
//                }
//            }
//
//            try {
//                p = (Postings) deserialize(content);
//                return p;
//            } catch (IOException io) {
//                System.out.println("Error: " + io.toString());
//                return null;
//            } catch (ClassNotFoundException cnf) {
//                System.out.println("Error: " + cnf.toString());
//            }
//
//        } catch (IOException e) {
//            System.out.println("I/O Error " + e);
//            return null;
//        }
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile("postings_records_in_binary_file", "r");

            byte[] content = new byte[postingsRecords.length];

            randomAccessFile.seek(postingsRecords.start);

            randomAccessFile.readFully(content);
            try {
                Postings p = (Postings) deserialize(content);
            } catch (IOException ioe) {
                System.out.println(ioe.toString());
            } catch (ClassNotFoundException cnf) {
                System.out.println(cnf.toString());
            }

        }catch (IOException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }

    private static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject(obj);
        return outputStream.toByteArray();
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
        System.out.println("Total related documents: " + map.size());
        int count = 0;
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
//        Integer resultsToShow = 20;
//        try {
//            resultsToShow = Integer.getInteger(args[0]);
//        } catch (ArrayIndexOutOfBoundsException oe) {
//            System.out.print("By default, the search will only show 20 most relevant results. ");
//            System.out.println("You can specify to show all the results by:\njava Dictionary -1\n");
//        } finally {
//            Dictionary d = new Dictionary(resultsToShow);
//
//            try {
//                d.loadIndexData();
//                Scanner inputScan = new Scanner(System.in);
//                while (true) {
//                    System.out.println("\nPlease input terms for searching");
//                    String input = inputScan.nextLine();
//                    d.searchWithTerms(input);
//                }
//            } catch (Exception e) {
//                System.out.println(e.toString());
//            }
//        }



        /**
         * Binary save and RandomAcessFile testing:
         */
        Postings p1 = new Postings();
        Postings p2 = new Postings();

        int start = 0;

        File recordsFile = new File("postingsRecords");

        for (int j = 2; j <= 20; j++) {
            p2.addItem(j);
        }

        for (int i = 1; i <= 20; i++) {
            p1.addItem(i);
        }
        System.out.println("The content of postings is:\n" + p1);

        System.out.println("Serialize it into binary file");

        try {
            int size =  Util.sizeof(p1);
            System.out.println("The size of postings is: " + size);

            FileOutputStream fos = new FileOutputStream(recordsFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(p1);
            oos.flush();
            oos.writeObject(p2);
            oos.flush();
            oos.close();
            fos.close();

//            RandomAccessFile rf = new RandomAccessFile("postingsRecords", "r");
            RandomAccessFile rf = new RandomAccessFile(recordsFile, "r");

            byte[] content = new byte[size];
            rf.seek(start);
            rf.readFully(content);

            p1 = null;
            try {
                System.out.println("\nBefore access, p1 is:\n" + p1);
                p1 = (Postings) deserialize(content);
                System.out.println("\nAfter access, p1 is:\n" + p1);
            } catch (IOException ioe) {
                System.out.println(ioe.toString());
            } catch (ClassNotFoundException cnf) {
                System.out.println(cnf.toString());
            }
            System.out.println();


            System.out.println("size of file: " + recordsFile.length());
//            start += size;

            // p2
//            System.out.println("\nprocess p2:");
//            size = Util.sizeof(p2);
//            System.out.println("the size of p2: " + size);
//            content = new byte[434];
//            System.out.println(content.length);
//            rf.seek(start);
//            rf.readFully(content);
//            rf.seek(0);
//            rf.readFully(content);

//            p2 = null;
//            try {
//                System.out.println("\nBefore access, p2 is:\n" + p2);
//                p2 = (Postings) deserialize(content);
//                System.out.println("\nAfter access, p2 is:\n" + p2);
//            } catch (IOException ioe) {
//                System.out.println(ioe.toString());
//            } catch (ClassNotFoundException cnf) {
//                System.out.println(cnf.toString());
//            }



        } catch (Exception e) {
            System.out.println(e.toString());
        }


    }
}