To compile, cd into the code directory, then:
javac -d ~/Downloads/tmp/  *.java
This will compile the source code into your ~/Downloads/tmp/ directory

Usage:
Assignment Part one, parse XML file (takes more than 600 seconds on my computer.):
java Parser <Your_XML_File>

This will generate 4 files in the current directory:
1.
"tokens.txt" lists all the tokens from each documents, separated by blank line. (For checking Assignment part one.)

2.
"indexed_terms_in_binary" stores the indexed term records which is a HashMap<Term, PostingsRecords>.

3.
"postings_records_for_DocIDs" stores the postings records of docIDs.

4.
"postings_records_for_TFs" stores the postings records of TFs.



Assignment Part two: after compile, cd into the generate class file directory, RUN:
java Dictionary by default

OR
java Dictionary <terms_records> <docIDs_records> <tfs_records> <num_of_results_to_show>

<terms_records> specify the indexed term records
<docIDs_records> specify the postings records of docIDs
<tfs_records> specify the postings records of tfs.
<num_of_results_to_show> specify the number of related documents to show. Because it uses TF-IDF ranking,
    the search results contains all the related documents which is a long list for some terms.
    To show all the results, using -1.
    Notice: Print out all the related search results will slow down the excution time. The default limit is showing top 20 results.